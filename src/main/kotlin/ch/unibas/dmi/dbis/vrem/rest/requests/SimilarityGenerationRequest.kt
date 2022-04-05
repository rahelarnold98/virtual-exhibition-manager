package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

/**
 * Request to generate a similarity room.
 *
 * @property roomSpec Room specification (width, height, etc.).
 * @property genType The generation type (i.e., the features) to use for room generation (must be defined in the configuration).
 * @property objectId The ID of the image to generate a similarity room for.
 */
@Serializable
data class SimilarityGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: String,
    val objectId: String
)
