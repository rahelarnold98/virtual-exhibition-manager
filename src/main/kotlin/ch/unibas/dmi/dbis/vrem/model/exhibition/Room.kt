package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.newId

/**
 * Object representation of exhibition rooms.
 *
 * @property id The ID of the room.
 * @property text The title/description of the room.
 * @property floor The floor texture of the room.
 * @property ceiling The ceiling texture of the room.
 * @property position The position of the room.
 * @property size The size of the room.
 * @property entryPoint The spawning/entry point of the room.
 * @property ambient The ambient sound of the room.
 * @property exhibits A list of exhibits present in the room.
 * @property walls A list of walls present in the room.
 *                 Note that, usually, exhibits are part of walls and not of the exhibit list of a room object.
 * @property metadata Miscellaneous metadata for the room for various purposes.
 */
@Serializable
data class Room(
    @SerialName("_id")
    @Schema(name = "_id") // OpenAPI spec.
    val id: String = newId<Exhibition>().toString(),
    var text: String = "",
    var floor: String = DEFAULT_FLOOR,
    var ceiling: String = DEFAULT_CEILING,
    var position: Vector3f = DEFAULT_POSITION,
    var size: Vector3f = DEFAULT_SIZE,
    var entryPoint: Vector3f = DEFAULT_ENTRYPOINT,
    var ambient: String? = null,
    var exhibits: MutableList<Exhibit> = mutableListOf(),
    var walls: MutableList<Wall> = mutableListOf(),
    var metadata: MutableMap<String, String> = mutableMapOf()
) {

    companion object {
        val DEFAULT_SIZE = Vector3f(10, 5, 10)
        val DEFAULT_ENTRYPOINT = Vector3f.ORIGIN
        val DEFAULT_POSITION = Vector3f.ORIGIN
        const val DEFAULT_FLOOR = "NONE"
        const val DEFAULT_CEILING = "NONE"
    }

    /**
     * Adds an exhibit to the room if it is not already in the room's list of exhibits.
     *
     * @param exhibit The exhibit to add.
     * @return True if the exhibit was successfully added, false otherwise (also in case of duplicates).
     */
    fun placeExhibit(exhibit: Exhibit): Boolean {
        if (exhibit.type != CulturalHeritageObject.Companion.CHOType.MODEL) {
            throw IllegalArgumentException("Only 3D objects can be placed in a room.")
        }
        return if (!exhibits.contains(exhibit)) {
            exhibits.add(exhibit)
        } else {
            false
        }
    }

    /**
     * Adds a wall with a given direction to the room.
     *
     * @param dir The direction of the wall.
     * @param w The wall to add.
     */
    fun setWall(dir: Direction, w: Wall) {
        if (w.direction != dir) {
            throw IllegalArgumentException("Wall direction not matching, expected $dir but got ${w.direction} instead.")
        }
        walls.add(w)
    }

    /**
     * Obtains the wall object for a given direction for the room.
     *
     * @param dir The direction of the wall.
     * @return The wall facing the provided direction in the room.
     */
    fun getWall(dir: Direction): Wall {
        try {
            return walls.first { w -> w.direction == dir }
        } catch (e: NoSuchElementException) {
            throw IllegalStateException("Corrupt room, missing direction $dir.")
        }
    }

}
