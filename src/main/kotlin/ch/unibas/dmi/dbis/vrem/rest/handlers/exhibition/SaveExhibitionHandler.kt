package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition

import ch.unibas.dmi.dbis.vrem.database.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class SaveExhibitionHandler(private val writer: VREMWriter) : PostRestHandler<ResponseMessage> {

    override val route: String
        get() = "/exhibitions/save"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Stores an exhibition to the database.",
        path = "/api/exhibitions/save",
        tags = ["Exhibition"],
        requestBody = OpenApiRequestBody(
            content = [OpenApiContent(Exhibition::class)],
            required = true,
            description = "An exhibition object as JSON string."
        ),
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): ResponseMessage {
        val exhibition = ctx.bodyAsClass<Exhibition>()

        return if (writer.saveExhibition(exhibition)) {
            ResponseMessage("Successfully saved exhibition.")
        } else {
            ResponseMessage("Failed to save exhibition.")
        }
    }

}
