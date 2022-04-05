package ch.unibas.dmi.dbis.vrem.rest.responses

import kotlinx.serialization.Serializable

/**
 * Generic response message object.
 *
 * @property message The message as a string.
 */
@Serializable
data class ResponseMessage(val message: String)
