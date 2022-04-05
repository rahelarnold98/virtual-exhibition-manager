package ch.unibas.dmi.dbis.vrem.rest.status

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Exception to wrap error messages including a [StatusCode] and a message.
 *
 * @property statusCode The status code for the message.
 * @property status The status to set.
 * @property log Whether to log this exception (defaults to true).
 */
data class ErrorStatusException(
    val statusCode: Int,
    val status: String,
    val log: Boolean = true
) : Exception(status) {

    init {
        if (log) {
            logger.info {
                "ErrorStatusException (code: $statusCode, message: $status) thrown by ${stackTrace.first()}."
            }
        }
    }

}
