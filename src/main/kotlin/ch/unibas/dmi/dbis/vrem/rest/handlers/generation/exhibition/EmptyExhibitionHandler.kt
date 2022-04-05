package ch.unibas.dmi.dbis.vrem.rest.handlers.generation.exhibition

import ch.unibas.dmi.dbis.vrem.generation.generators.ExhibitionGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.HttpMethod
import io.javalin.plugin.openapi.annotations.OpenApi
import io.javalin.plugin.openapi.annotations.OpenApiContent
import io.javalin.plugin.openapi.annotations.OpenApiResponse

class EmptyExhibitionHandler : PostRestHandler<Exhibition> {

    override val route: String = "/generate/exhibition"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Generates a new, empty exhibition.",
        path = "/api/generate/exhibition",
        tags = ["Generation"],
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(Exhibition::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): Exhibition {
        return ExhibitionGenerator().genExhibition()
    }

}
