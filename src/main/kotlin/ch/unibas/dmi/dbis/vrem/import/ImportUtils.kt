package ch.unibas.dmi.dbis.vrem.import

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import java.io.ByteArrayInputStream
import javax.imageio.ImageIO

/**
 * Utilities used for exhibition import.
 */
object ImportUtils {

    const val WALL_CONFIG_FILE = "wall-config.json"
    const val ROOM_CONFIG_FILE = "room-config.json"

    const val JSON_EXTENSION = "json"
    const val JPG_EXTENSION = "jpg"
    const val JPEG_EXTENSION = "jpeg"
    const val PNG_EXTENSION = "png"
    const val BMP_EXTENSION = "bmp"

    val IMAGE_FILE_EXTENSIONS = listOf(JPEG_EXTENSION, JPG_EXTENSION, PNG_EXTENSION, BMP_EXTENSION)

    /**
     * Calculates the room position depending on the number of siblings, arranging rooms in a line.
     *
     * @param room The room to calculate the position for.
     * @param siblings The list of all current rooms.
     * @return The vector of the room's position.
     */
    fun calculateRoomPosition(room: Room, siblings: List<Room>): Vector3f {
        // Automated room position calculation: Use origin if unspecified.
        if (siblings.isEmpty()) {
            return Vector3f(0.0f, 0.0f, 0.0f)
        }

        val offset = 1.0f
        val last = siblings.last()
        val dist = last.position.x + 0.5 * last.size.x + 0.5 * room.size.x + offset

        return Vector3f(dist, 0.0f, 0.0f)
    }

    /**
     * Calculates the exhibit position on the wall depending on the number of siblings that have already been added.
     * Starts on the left-hand side of the wall and fills up towards the right.
     * Note that, if there are too many exhibits, the images can get placed outside of the wall.
     *
     * @param exhibit The exhibit to calculate the position for.
     * @param siblings The siblings of the exhibit on the same wall.
     * @param roomBorder The border of the room (x offset).
     * @param exhibitPadding Padding between exhibits.
     * @param exhibitHeight Height (y) the exhibit is placed at on the wall.
     * @return The vector of the exhibit's position.
     */
    fun calculateWallExhibitPosition(
        exhibit: Exhibit,
        siblings: List<Exhibit>,
        roomBorder: Float = .5f,
        exhibitPadding: Float = 1f,
        exhibitHeight: Float = 1.5f
    ): Vector3f {
        return if (siblings.isEmpty()) {
            Vector3f(roomBorder + (exhibit.size.x / 2f), exhibitHeight)
        } else {
            val dist = siblings.map { it.size.x + exhibitPadding }.sum()
            Vector3f(roomBorder + dist + (exhibit.size.x / 2f), exhibitHeight)
        }
    }

    fun calculateExhibitSize(exhibitFile: ByteArray, exhibit: Exhibit, defaultLongSide: Float) {
        // Load image as stream and only check for width/height we don't have to load the entire thing.
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
            height = (aspectRatio * (defaultLongSide * 100f)) / 100f // Convert to cm for precision.
        } else {
            width = ((defaultLongSide * 100f) / aspectRatio) / 100f // Convert to cm for precision.
        }

        // Update width/height.
        exhibit.size = Vector3f(width, height)
    }

    /**
     * Copies the description from one exhibit to another.
     *
     * @param src The exhibit to serve as source.
     * @param dest The destination exhibit to copy the description to.
     * @param overwrite Whether to overwrite existing descriptions in the destination exhibit.
     */
    fun copyDescription(src: Exhibit, dest: Exhibit, overwrite: Boolean = false) {
        if (src.description.isNotBlank()) {
            if (overwrite || dest.description.isBlank()) {
                dest.description = src.description
            }
        }
    }

    /**
     * Copies the name from one exhibit to another.
     *
     * @param src The exhibit to serve as source.
     * @param dest The destination exhibit to copy the name to.
     * @param overwrite Whether to overwrite existing name in the destination exhibit.
     */
    fun copyName(src: Exhibit, dest: Exhibit, overwrite: Boolean = false) {
        if (src.name.isNotBlank()) {
            if (overwrite || dest.name.isBlank()) {
                dest.name = src.name
            }
        }
    }

    /**
     * Searches an exhibition and its rooms for an exhibit with a given path.
     *
     * @param exhibition The exhibition to find the exhibit for.
     * @param path The path relative to the exhibition to find the exhibit at.
     * @return The exhibit if it could be found, null otherwise.
     */
    fun findExhibitForPath(exhibition: Exhibition, path: String): Exhibit? {
        exhibition.rooms.forEach { room ->
            room.walls.flatMap { it.exhibits }.forEach {
                if (path == it.path) {
                    return it
                }
            }
        }
        return null
    }

}
