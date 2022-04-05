package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibit

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ListExhibitsResponse
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.HttpMethod
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiResponse

class ListExhibitsHandler(private val reader: VREMReader) : GetRestHandler<ListExhibitsResponse> {

    override val route: String
        get() = "/exhibits/list"

    @OpenApi(
        method = HttpMethod.GET,
        summary = "Lists all exhibits stored in the database.",
        path = "/api/exhibits/list",
        tags = ["Exhibit"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ListExhibitsResponse::class)]),
            OpenApiResponse(StatusCode.INTERNAL_SERVER_ERROR.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doGet(ctx: Context): ListExhibitsResponse {
        return ListExhibitsResponse(reader.listExhibits())
    }

}
