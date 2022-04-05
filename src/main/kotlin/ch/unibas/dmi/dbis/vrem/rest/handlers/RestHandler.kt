package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.extensions.errorResponse
import ch.unibas.dmi.dbis.vrem.rest.status.ErrorStatusException
import io.javalin.http.Context

interface RestHandler {

    val route: String

}

interface GetRestHandler<T : Any> : RestHandler {

    fun get(ctx: Context) {
        try {
            ctx.json(doGet(ctx))
        } catch (e: ErrorStatusException) {
            ctx.errorResponse(e)
        } catch (e: Exception) {
            ctx.errorResponse(500, e.message ?: "")
        }
    }

    fun doGet(ctx: Context): T

}

interface PostRestHandler<T : Any> : RestHandler {

    fun post(ctx: Context) {
        try {
            ctx.json(doPost(ctx))
        } catch (e: ErrorStatusException) {
            ctx.errorResponse(e)
        } catch (e: Exception) {
            ctx.errorResponse(500, e.message ?: "")
        }
    }

    fun doPost(ctx: Context): T

}

interface PutRestHandler<T : Any> : RestHandler {

    fun put(ctx: Context) {
        try {
            ctx.json(doPut(ctx))
        } catch (e: ErrorStatusException) {
            ctx.errorResponse(e)
        } catch (e: Exception) {
            ctx.errorResponse(500, e.message ?: "")
        }
    }

    fun doPut(ctx: Context): T

}

interface DeleteRestHandler<T : Any> : RestHandler {

    fun delete(ctx: Context) {
        try {
            ctx.json(doDelete(ctx))
        } catch (e: ErrorStatusException) {
            ctx.errorResponse(e)
        } catch (e: Exception) {
            ctx.errorResponse(500, e.message ?: "")
        }
    }

    fun doDelete(ctx: Context): T

}
