package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class LoadExhibitionByNameHandler(private val reader: VREMReader) : GetRestHandler<Exhibition> {

    companion object {

        const val PARAM_KEY_NAME = "{name}"

    }

    override val route: String
        get() = "/exhibitions/load/name/${PARAM_KEY_NAME}"

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Loads an exhibition by name.",
        path = "/api/exhibitions/load/name/{name}",
        pathParams = [
            OpenApiParam(
                name = "id",
                type = String::class,
                description = "The name of the exhibition to load.",
                required = true
            )
        ],
        tags = ["Exhibition"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(Exhibition::class)]),
            OpenApiResponse(StatusCode.NOT_FOUND.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doGet(ctx: Context): Exhibition {
        val name = ctx.pathParam(PARAM_KEY_NAME)
        return reader.getExhibitionByName(name)
    }

}
