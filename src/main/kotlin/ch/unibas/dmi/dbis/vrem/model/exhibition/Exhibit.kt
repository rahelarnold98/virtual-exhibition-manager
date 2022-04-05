package ch.unibas.dmi.dbis.vrem.model.exhibition

import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.newId

/**
 * Exhibit object.
 *
 * @property id The ID of the exhibit (defaulted to the object's ID as hex string).
 * @property name The name of the exhibit.
 * @property description The description of the exhibit.
 * @property path The path of the exhibit.
 * @property type The type of the exhibit from the CHO type enum.
 * @property size The size of the exhibit as a vector.
 * @property position The position of the exhibit as a vector.
 * @property audio The name of the audio for the exhibit as a string.
 * @property light Whether the exhibit has light or not.
 * @property metadata Miscellaneous metadata for the exhibit for various purposes.
 */
@Serializable
data class Exhibit(
    @SerialName("_id")
    @Schema(name = "_id") // OpenAPI spec.
    override val id: String = newId<Exhibit>().toString(),
    override var name: String,
    override var description: String = "",
    override var path: String = "",
    override var type: CulturalHeritageObject.Companion.CHOType = DEFAULT_TYPE,
    var size: Vector3f = DEFAULT_SIZE,
    var position: Vector3f = DEFAULT_POSITION,
    var audio: String? = null,
    var light: Boolean = false,
    var metadata: MutableMap<String, String> = mutableMapOf()
) : CulturalHeritageObject() {

    constructor(name: String, path: String, choType: CulturalHeritageObject.Companion.CHOType) : this(
        name = name,
        path = path,
        type = choType
    )

    companion object {
        const val URL_ID_SUFFIX = ".remote"

        val DEFAULT_SIZE = Vector3f.UNIT
        val DEFAULT_POSITION = Vector3f.ORIGIN
        val DEFAULT_TYPE = CulturalHeritageObject.Companion.CHOType.IMAGE

        /**
         * Creates a copy of an exhibit and returns the newly created object.
         *
         * @param e The exhibit to copy.
         * @return The newly created copy of the exhibit.
         */
        fun copy(e: Exhibit): Exhibit {
            return Exhibit(
                newId<Exhibit>().toString(),
                e.name,
                e.description,
                e.path,
                e.type,
                e.position,
                e.size,
                e.audio,
                e.light
            )
        }
    }

}
