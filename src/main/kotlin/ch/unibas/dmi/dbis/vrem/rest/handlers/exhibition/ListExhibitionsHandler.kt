package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ListExhibitionsResponse
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.HttpMethod
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiResponse

class ListExhibitionsHandler(private val reader: VREMReader) : GetRestHandler<ListExhibitionsResponse> {

    override val route: String
        get() = "/exhibitions/list"

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Lists the IDs and names of all exhibitions stored in the database.",
        path = "/api/exhibitions/list",
        tags = ["Exhibition"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ListExhibitionsResponse::class)]),
            OpenApiResponse(StatusCode.INTERNAL_SERVER_ERROR.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doGet(ctx: Context): ListExhibitionsResponse {
        return ListExhibitionsResponse(reader.listExhibitions())
    }

}
