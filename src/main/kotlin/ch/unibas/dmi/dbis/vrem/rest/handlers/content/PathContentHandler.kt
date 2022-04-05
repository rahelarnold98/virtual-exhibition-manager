package ch.unibas.dmi.dbis.vrem.rest.handlers.content

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import java.nio.file.Path

class PathContentHandler(docRoot: Path, cineastConfig: CineastConfig) :
    ContentHandler(docRoot, cineastConfig),
    GetRestHandler<Any> {

    companion object {

        const val PARAM_KEY_PATH = "{path}"

    }

    override val route: String
        get() = "/content/${PARAM_KEY_PATH}"

    override fun doGet(ctx: Context): Any = "" // Not used, we return bytes via get() instead.

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Retrieves exhibit content (e.g., an image) based on a url via path parameter.",
        path = "/api/content/{path}",
        pathParams = [
            OpenApiParam(
                name = "path",
                type = String::class,
                description = "The path of the exhibit's content.",
                required = true
            )
        ],
        tags = ["Content"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ByteArray::class)]),
            OpenApiResponse(StatusCode.INTERNAL_SERVER_ERROR.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun get(ctx: Context) {
        val path = ctx.pathParam(PARAM_KEY_PATH)
        getContent(ctx, path)
    }

}
