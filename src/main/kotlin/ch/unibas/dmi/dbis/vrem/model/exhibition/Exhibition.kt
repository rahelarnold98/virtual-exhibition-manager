package ch.unibas.dmi.dbis.vrem.model.exhibition

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.newId
import java.util.*

/**
 * Exhibition object.
 *
 * @property id The ID of the exhibition (using kmongo to create the ID).
 * @property name The name of the exhibition.
 * @property description The description of the exhibition.
 * @property rooms A list of rooms that the exhibition consists of.
 * @property metadata Miscellaneous metadata for the exhibition for various purposes.
 */

@Serializable
data class Exhibition(
    @SerialName("_id")
    @Schema(name = "_id") // OpenAPI spec.
    val id: String = newId<Exhibition>().toString(),
    val name: String,
    val description: String = "",
    val rooms: MutableList<Room> = mutableListOf(),
    val metadata: MutableMap<String, String> = mutableMapOf()
) {

    /**
     * Adds a room to the exhibition if it is not already present.
     *
     * @param room The room to add.
     * @return True if the room was successfully added, false otherwise.
     */
    fun addRoom(room: Room): Boolean {
        return if (!rooms.contains(room)) {
            rooms.add(room)
        } else {
            false
        }
    }

    /**
     * Obtains all exhibits from all rooms of the exhibition.
     *
     * @return A list of all exhibits.
     */
    fun obtainExhibits(): MutableList<Exhibit> {
        val exhibits = mutableListOf<Exhibit>()

        rooms.forEach { r ->
            exhibits.addAll(r.exhibits)

            r.walls.forEach { w ->
                exhibits.addAll(w.exhibits)
            }
        }

        return Collections.unmodifiableList(exhibits)
    }

    /**
     * Obtains all exhibits of a certain type from the exhibition.
     *
     * @param type The CHO type to obtain.
     * @return A list of all exhibits of the provided type.
     */
    fun obtainExhibits(type: CulturalHeritageObject.Companion.CHOType): List<Exhibit> {
        return obtainExhibits().filter { e -> e.type == type }
    }

}
