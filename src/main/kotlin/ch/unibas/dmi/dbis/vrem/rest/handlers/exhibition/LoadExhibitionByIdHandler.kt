package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class LoadExhibitionByIdHandler(private val reader: VREMReader) : GetRestHandler<Exhibition> {

    companion object {

        const val PARAM_KEY_ID = "{id}"

    }

    override val route: String
        get() = "/exhibitions/load/id/$PARAM_KEY_ID"

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Loads an exhibition by ID.",
        path = "/api/exhibitions/load/id/{id}",
        pathParams = [
            OpenApiParam(
                name = "id",
                type = String::class,
                description = "The ID of the exhibition to load.",
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
        val id = ctx.pathParam(PARAM_KEY_ID)
        return reader.getExhibitionById(id)
    }

}
