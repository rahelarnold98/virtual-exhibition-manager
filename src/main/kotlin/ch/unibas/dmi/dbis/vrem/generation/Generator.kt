package ch.unibas.dmi.dbis.vrem.generation

import ch.unibas.dmi.dbis.vrem.generation.model.DoubleFeatureData
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit.Companion.URL_ID_SUFFIX
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.requests.GenerationRequest
import java.io.ByteArrayInputStream
import java.time.LocalDateTime
import javax.imageio.ImageIO
import kotlin.math.max

abstract class Generator(
    val genConfig: GenerationRequest,
    val cineastHttp: CineastHttp
) {

    abstract val roomText: String

    abstract fun genRoom(): Room

    val exhibitionText: String = "Generated Exhibition"

    fun getTextSuffix(): LocalDateTime? = LocalDateTime.now()

    open fun genExhibition(): Exhibition {
        // Generic exhibition generation function, can be overwritten.
        val room = genRoom()

        val ex = Exhibition(name = exhibitionText + " " + getTextSuffix())
        ex.rooms.add(room)
        ex.metadata[MetadataType.GENERATED.key] = true.toString()

        return ex
    }

    protected fun getFeatures(): DoubleFeatureData {
        val featureDataList = arrayListOf<DoubleFeatureData>()

        for (tablePair in genConfig.genType.featureList) {
            val featureData = CineastClient.getFeatureDataFromTableName(tablePair.id, genConfig.idList)

            // Normalize data.
            featureData.normalize(tablePair.value)

            featureDataList.add(featureData)
        }

        return DoubleFeatureData.concatenate(featureDataList)
    }

    protected fun idListToExhibits(ids: List<String?>): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        for (id in ids) {
            var e: Exhibit

            if (id == null) {
                e = Exhibit(name = "Empty Exhibit", path = "")
                e.size = Vector3f(1.0, 1.0)
            } else {
                val cleanId = CineastClient.cleanId(id)

                e = Exhibit(name = cleanId, path = cleanId + URL_ID_SUFFIX)

                val imageBytes = cineastHttp.objectRequest(cleanId)

                calculateExhibitSize(imageBytes, e, 2.0)

                // Set generation metadata.
                e.metadata[MetadataType.GENERATED.key] = "true"
            }

            exhibits.add(e)
        }

        return exhibits
    }

    protected fun exhibitListToWalls(dims: IntArray, exhibitList: MutableList<Exhibit>): MutableList<Wall> {
        val walls = mutableListOf<Wall>()

        // Add a wall for every direction.
        enumValues<Direction>().forEach { e ->
            walls.add(Wall(e, "CONCRETE"))
        }

        // Place exhibits on walls (this will only work for 2D setups with 4 walls).
        for (i in 0 until dims[0]) {
            for (w in 0 until walls.size) {
                for (j in 0 until dims[1] / walls.size) {
                    val exhibit = exhibitList.removeFirst()

                    exhibit.position = getWallPositionByCoords(i, j)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        return walls
    }

    protected fun wallsToRoom(walls: List<Wall>): Room {
        val room = Room(text = roomText + " " + getTextSuffix())

        // Set room size and add walls.
        room.size = getRoomDimFromWalls(walls)
        room.walls.addAll(walls)

        // Add metadata to room.
        room.metadata[MetadataType.GENERATED.key] = true.toString()
        room.metadata[MetadataType.SEED.key] = genConfig.seed.toString()

        return room
    }

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

    // TODO Let the user define the padding/base height/longer side length and put it in a config.
    fun getWallPositionByCoords(
        row: Int,
        column: Int,
        padding: Double = 1.5, // Towards other exhibits and walls.
        baseHeight: Double = 2.0, // Offset towards the ground.
        longerSideLength: Double = 2.0 // Maximum length/height of an exhibit.
    ): Vector3f {
        return Vector3f(
            (longerSideLength + padding) * column + padding,
            (longerSideLength + padding) * row + baseHeight,
            0.0
        )
    }

    fun getRoomDimFromWalls(
        walls: List<Wall>,
        padding: Double = 1.5,
        baseHeight: Double = 2.0
    ): Vector3f {
        var xDimLen = 0.0
        var yDimLen = 0.0
        var zDimLen = 0.0

        for (w in walls) {
            val lastPos = w.exhibits.last().position

            // Use exhibit x coordinate for both dimensions since it's relative to the wall, and not to the world.
            if (w.direction.axis == Direction.Coordinate.X) {
                xDimLen = max(xDimLen, lastPos.x + padding)
            } else if (w.direction.axis == Direction.Coordinate.Z) {
                zDimLen = max(zDimLen, lastPos.x + padding)
            }

            yDimLen = max(yDimLen, lastPos.y.toDouble() + baseHeight)
        }

        return Vector3f(xDimLen, yDimLen, zDimLen)
    }

}
