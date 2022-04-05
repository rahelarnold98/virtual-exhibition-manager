package ch.unibas.dmi.dbis.vrem.rest.handlers.content

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit.Companion.URL_ID_SUFFIX
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import io.javalin.http.Context
import java.io.ByteArrayInputStream
import java.net.URLConnection
import java.nio.file.Files
import java.nio.file.Path

abstract class ContentHandler(private val docRoot: Path, private val cineastConfig: CineastConfig) {

    private fun setHeader(ctx: Context) {
        ctx.header("Transfer-Encoding", "identity")
        ctx.header("Access-Control-Allow-Origin", "*")
        ctx.header("Access-Control-Allow-Headers", "*")
    }

    protected fun getContent(ctx: Context, path: String) {
        if (path.isBlank()) {
            ctx.status(404)
            return
        }

        val res: Boolean = if (path.endsWith(URL_ID_SUFFIX)) {
            getFromRemote(ctx, path)
        } else {
            getFromLocal(ctx, path)
        }

        if (res) {
            ctx.status(200)
        } else {
            ctx.status(404)
        }
    }

    protected fun getFromLocal(ctx: Context, path: String): Boolean {
        val absolute = docRoot.resolve(path)

        if (!Files.exists(absolute)) {
            return false
        }

        setHeader(ctx)
        ctx.contentType(Files.probeContentType(absolute))
        ctx.result(absolute.toFile().inputStream())

        return true
    }

    protected fun getFromRemote(ctx: Context, path: String): Boolean {
        // ID is composed as exhibitionID/imageID.remote.
        var resultBytes: ByteArray? = null
        val id = path.substring(path.indexOf("/") + 1, path.indexOf(URL_ID_SUFFIX))

        val (_, _, result) = cineastConfig.getCineastObjectUrlString(id).httpGet().response()

        when (result) {
            is Result.Failure -> return false
            is Result.Success -> resultBytes = result.get()
        }

        setHeader(ctx)
        ctx.contentType(URLConnection.guessContentTypeFromStream(ByteArrayInputStream(resultBytes)))
        ctx.result(resultBytes)

        return true
    }

}
