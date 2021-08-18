package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

/**
 * Cultural heritage object.
 *
 * @property id The ID of the object as a string.
 * @property name The name of the object.
 * @property type The object type as described in the enum.
 * @property path The path to the object.
 * @property description The description of the object.
 */
@Serializable
abstract class CulturalHeritageObject {

    @SerialName("_id")
    @JsonNames("id", "_id")
    abstract val id: String
    abstract var name: String
    abstract var type: CHOType
    abstract var path: String
    abstract var description: String

    companion object {
        /**
         * Types of Cultural Heritage Objects.
         */
        enum class CHOType {
            IMAGE, MODEL, VIDEO, MODEL_STRUCTURAL
        }
    }

}
