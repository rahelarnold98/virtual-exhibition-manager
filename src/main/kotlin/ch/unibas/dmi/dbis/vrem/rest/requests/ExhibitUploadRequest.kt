package ch.unibas.dmi.dbis.vrem.rest.requests

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.Serializable

/**
 * Exhibit upload request.
 *
 * @property artCollection The name of the art collection.
 * @property exhibit The exhibit object to upload.
 * @property file The file (path) of the exhibit.
 * @property fileExtension The file extension of the exhibit.
 */
@Serializable
data class ExhibitUploadRequest(
    val artCollection: String,
    val exhibit: Exhibit,
    val file: String,
    val fileExtension: String
)
