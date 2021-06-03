package ch.unibas.dmi.dbis.vrem.model.api.request

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.Serializable

/**
 * TODO: Write JavaDoc
 * @author loris.sauter
 */
@Serializable
data class ExhibitUploadRequest(
    val artCollection: String,
    val exhibit: Exhibit,
    val file: String,
    val fileExtension: String
)
