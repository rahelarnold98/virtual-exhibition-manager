package ch.unibas.dmi.dbis.vrem.model.collection

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.litote.kmongo.newId

/**
 * Art collection object.
 *
 * @property id The ID of the art collection (as stored in MongoDB).
 * @property name The name of the collection.
 * @property exhibits The exhibits present in the collection.
 * @property metadata Mapping of metadata attributes to descriptions for the collection.
 */
@Serializable
data class ArtCollection(
    @SerialName("_id")
    val id: String = newId<ArtCollection>().toString(),
    val name: String,
    val exhibits: List<Exhibit> = mutableListOf(),
    val metadata: Map<String, String> = mutableMapOf()
)
