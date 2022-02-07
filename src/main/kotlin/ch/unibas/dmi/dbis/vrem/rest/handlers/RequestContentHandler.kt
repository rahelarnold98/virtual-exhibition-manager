package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.javalin.http.Context
import mu.KotlinLogging
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path

private val logger = KotlinLogging.logger {}

/**
 * Handler for content requests made through the API.
 *
 * @property docRoot The document root of the exhibition.
 */
class RequestContentHandler(private val docRoot: Path, private val cineastConfig: CineastConfig) {

    companion object {
        const val PARAM_KEY_PATH = ":path"
        const val URL_ID_SUFFIX = ".remote"
    }

    fun serveContentBody(ctx: Context) {
        val path = ctx.queryParam("path") ?: ""
        serve(ctx, path)
    }


    /**
     * Serves the requested content.
     *
     * TODO Clean this up!
     *
     * @param ctx The Javalin request context.
     */
    fun serveContent(ctx: Context) {
        val path = ctx.pathParam(PARAM_KEY_PATH)
        serve(ctx, path)
    }

    private fun serve(ctx: Context, path: String) {

        if (path.isBlank()) {
            logger.error { "The requested path was blank - did you forget to send the actual content path?" }
            ctx.status(404)
            return
        }

        if (path.endsWith(URL_ID_SUFFIX)) {
            // ID is composed as exhibitionID/imageID.remote for cineast
            val id = path.substring(path.indexOf("/") + 1, path.indexOf(URL_ID_SUFFIX))
            var resultBytes: ByteArray? = null

            logger.info { "Trying to serve object with ID $id." }

            val (_, _, result) = cineastConfig.getCineastObjectUrlString(id).httpGet().response()

            when (result) {
                is Result.Failure -> {
                    val ex = result.getException()
                    logger.error { "Cannot serve object with ID $id from exhibition $ex." }
                    ctx.status(404)
                    return
                }
                is Result.Success -> {
                    resultBytes = result.get()
                }
            }

            ctx.contentType(URLConnection.guessContentTypeFromStream(ByteArrayInputStream(resultBytes)))
            ctx.header("Transfer-Encoding", "identity")
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")

            ctx.result(resultBytes)
        } else {
            val absolute = docRoot.resolve(path)

            if (!Files.exists(absolute)) {
                logger.error { "Cannot serve $absolute as it does not exist." }
                ctx.status(404)
                return
            }
            val content = Files.probeContentType(absolute)
            ctx.contentType(content)
            ctx.header("Content-Type", content);
            ctx.header("Transfer-Encoding", "identity")
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")

            logger.info { "Serving $absolute as $content" }

            ctx.result(absolute.toFile().inputStream())
        }
    }


}
