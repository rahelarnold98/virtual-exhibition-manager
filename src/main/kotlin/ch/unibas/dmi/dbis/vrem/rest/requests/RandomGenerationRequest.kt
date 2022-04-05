package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

/**
 * Request to generate a random room.
 *
 * @property roomSpec Room specification (width, height, etc.).
 * @property idList A list of IDs to use for the randomization.
 * @property seed The seed to use for randomization.
 */
@Serializable
data class RandomGenerationRequest(
    val roomSpec: RoomSpecification,
    val idList: ArrayList<String>,
    val seed: Int
)
