package ch.unibas.dmi.dbis.vrem.extensions

import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.ErrorStatusException
import io.javalin.http.Context

/**
 * Adds an error message and a status to a context.
 *
 * @param status The status code to set.
 * @param errorMessage The error message to add.
 */
fun Context.errorResponse(status: Int, errorMessage: String) {
    this.status(status)
    this.json(ResponseMessage(errorMessage))
}

/**
 * Adds an [ErrorStatusException] to a context.
 *
 * @param error The error status exception to add the status code and status message from.
 */
fun Context.errorResponse(error: ErrorStatusException) {
    this.status(error.statusCode)
    this.json(ResponseMessage(error.status))
}
