package ch.unibas.dmi.dbis.vrem.generation.generators

import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastHttp
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit.Companion.URL_ID_SUFFIX
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO
import kotlin.math.max

/**
 * Generic room generator.
 *
 * @property cineastHttp Cineast HTTP client to obtain data.
 */
abstract class RoomGenerator(
        val cineastHttp: CineastHttp,
        val minWidth: Double = 5.0
) : ExhibitionGenerator() {


    abstract val roomText: String

    /**
     * Generates a new room.
     *
     * @return The newly generated room.
     */
    abstract fun genRoom(): Room

    /**
     * Converts a list of IDs to exhibit objects.
     *
     * @param ids The IDs to use.
     * @return A list of the created exhibits for each ID.
     */
    protected fun idListToExhibits(ids: List<String?>): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        for (id in ids) {
            var e: Exhibit

            if (id == null) {
                e = Exhibit(name = "Empty Exhibit", path = "")
                e.size = Vector3f(1.0, 1.0)
            } else {
                val cleanId = CineastClient.cleanId(id) // Hide segment number in exhibit label name.

                e = Exhibit(name = cleanId, path = cleanId + URL_ID_SUFFIX)

                val imageBytes = cineastHttp.objectRequest(cleanId)

                calculateExhibitSize(imageBytes, e, 2.0)

                // Set generation metadata.
                e.metadata[MetadataType.GENERATED.key] = "true"
                e.metadata[MetadataType.OBJECT_ID.key] = id // Set original ID to maintain this information.
            }

            exhibits.add(e)
        }

        return exhibits
    }

    /**
     * Returns a list of walls for every compass direction.
     *
     * @return The list of walls.
     */
    protected fun wallEnumToList(): MutableList<Wall> {
        val walls = mutableListOf<Wall>()

        // Add a wall for every direction.
        enumValues<Direction>().forEach { e ->
            walls.add(Wall(e, "CONCRETE"))
        }

        return walls
    }

    /**
     * Assigns a list of exhibits to walls, calculating their position on the walls.
     *
     * @param dims The dimension of the image grid (to be specified by VREP).
     * @param exhibitList The list of the exhibits to use.
     * @param ignoreEmptyExhibits Whether to silently ignore exhibits that do not have a path assigned.
     * @return A list of walls with exhibits and their positions assigned.
     */
    protected fun exhibitListToWalls(
            dims: IntArray,
            exhibitList: MutableList<Exhibit>,
            ignoreEmptyExhibits: Boolean = true
    ): MutableList<Wall> {
        val walls = wallEnumToList()

        // Place exhibits on walls (this will only work for 2D setups with 4 walls).
        for (i in 0 until dims[0]) {
            for (w in 0 until walls.size) {
                for (j in 0 until dims[1] / walls.size) {
                    if (exhibitList.isEmpty()) {
                        break
                    }

                    val exhibit = exhibitList.removeFirst()

                    if (exhibit.path == "" && ignoreEmptyExhibits) {
                        continue
                    }

                    exhibit.position = getWallPositionByCoords(i, j, dims[1] / walls.size == 1)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        return walls
    }

    /**
     * Creates a room object based on a list of walls, calculating the position based on them.
     *
     * @param walls The list of walls to use
     * @return The created room object.
     */
    protected fun wallsToRoom(walls: List<Wall>): Room {
        val room = Room(text = roomText + " " + getTextSuffix())

        // Set room size and add walls.
        room.size = getRoomDimFromWalls(walls)
        room.walls.addAll(walls)

        // Add metadata to room.
        room.metadata[MetadataType.GENERATED.key] = true.toString()

        return room
    }

    /**
     * Calculates the size of exhibits based on the image they hold.
     *
     * @param exhibitFile The exhibit file as bytes.
     * @param exhibit The exhibit to calculate the size for.
     * @param defaultLongSide The default length of the long side to use/scale for.
     */
    fun calculateExhibitSize(exhibitFile: ByteArray, exhibit: Exhibit, defaultLongSide: Double) {
        // Load image as stream so that we don't have to load the entire thing.
        val imageStream = ImageIO.createImageInputStream(ByteArrayInputStream(exhibitFile))

        val reader = ImageIO.getImageReaders(imageStream).next()
        reader.input = imageStream

        // Get width and height.
        val imageWidth = reader.getWidth(0)
        val imageHeight = reader.getHeight(0)

        // Close stream.
        imageStream.close()

        // Calculate aspect ratio and adjust width/height accordingly.
        val aspectRatio = imageHeight.toFloat() / imageWidth

        var width = defaultLongSide
        var height = defaultLongSide

        if (imageWidth > imageHeight) {
            height = (aspectRatio * (defaultLongSide * 100.0)) / 100.0 // Convert to cm for precision.
        } else {
            width = ((defaultLongSide * 100.0) / aspectRatio) / 100.0 // Convert to cm for precision.
        }

        // Update width/height.
        exhibit.size = Vector3f(width, height)
    }

    /**
     * Calculates the wall position of an exhibit based on its coordinates in the image grid.
     *
     * @param row The row of the exhibit in the image grid.
     * @param column The column of the exhibit in the image grid.
     * @param padding The padding to use between the exhibits.
     * @param baseHeight The base height above the ground to use as height offset.
     * @param longerSideLength The length of the longer side.
     * @return A vector holding the calculated position.
     */
    fun getWallPositionByCoords(
            row: Int,
            column: Int,
            minSize: Boolean = false,
            padding: Double = 1.5, // Towards other exhibits and walls.
            baseHeight: Double = 2.0, // Offset towards the ground.
            longerSideLength: Double = 2.0 // Maximum length/height of an exhibit.
    ): Vector3f {
        // TODO Let the user define the padding/base height/longer side length and put it in a config.

        if (minSize) {
            return Vector3f(
                    (minWidth / 2),
                    (longerSideLength + padding) * row + baseHeight,
                    0.0
            )
        }
        return Vector3f(
                (longerSideLength + padding) * column + padding,
                (longerSideLength + padding) * row + baseHeight,
                0.0
        )

    }

    /**
     * Calculates the dimension of a room based on a list of walls.
     *
     * @param walls The walls to use for the calculation.
     * @param padding The padding to use after the last image (this should be the same as in [getWallPositionByCoords]).
     * @param baseHeight The base height of the images, used as offset for height (should also be the same as in [getWallPositionByCoords]).
     * @return A vector holding the calculated position.
     */
    fun getRoomDimFromWalls(
        walls: List<Wall>,
        padding: Double = 1.5,
        baseHeight: Double = 2.0
    ): Vector3f {
        var xDimLen = 0.0
        var yDimLen = 0.0
        var zDimLen = 0.0

        for (w in walls) {
            if (w.exhibits.isEmpty()) {
                continue
            }

            val lastPos = w.exhibits.last().position

            // Use exhibit x coordinate for both dimensions since it's relative to the wall, and not to the world.
            if (w.direction.axis == Direction.Coordinate.X) {
                xDimLen = max(xDimLen, lastPos.x + padding)
            } else if (w.direction.axis == Direction.Coordinate.Z) {
                zDimLen = max(zDimLen, lastPos.x + padding)
            }

            yDimLen = max(yDimLen, lastPos.y.toDouble() + baseHeight)
        }

        // Set a minimum room size
        if (xDimLen < minWidth) {
            xDimLen = minWidth
        };
        if (zDimLen < minWidth) {
            zDimLen = minWidth
        };

        return Vector3f(xDimLen, yDimLen, zDimLen)
    }

}
