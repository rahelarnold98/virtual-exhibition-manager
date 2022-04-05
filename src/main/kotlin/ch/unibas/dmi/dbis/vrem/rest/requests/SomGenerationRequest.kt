package ch.unibas.dmi.dbis.vrem.rest.requests

import kotlinx.serialization.Serializable

/**
 * Request to generate a cluster room using the self-organizing map algorithm.
 *
 * @property roomSpec Room specification (width, height, etc.).
 * @property genType The generation type (i.e., the features) to use for room generation (must be defined in the configuration).
 * @property idList A list of IDs to use for the randomization.
 * @property seed The seed to use for randomization.
 * @property numEpochs The number of epochs to train the self-organizing map for.
 */
@Serializable
data class SomGenerationRequest(
    val roomSpec: RoomSpecification,
    val genType: String,
    val idList: ArrayList<String>,
    val seed: Int,
    val numEpochs: Int
)
