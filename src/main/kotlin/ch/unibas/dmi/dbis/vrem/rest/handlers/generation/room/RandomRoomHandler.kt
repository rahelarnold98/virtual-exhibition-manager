package ch.unibas.dmi.dbis.vrem.rest.handlers.generation.room

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.generators.RandomRoomGenerator
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.requests.RandomGenerationRequest
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*

class RandomRoomHandler(cineastConfig: CineastConfig) : PostRestHandler<Room> {

    private val cineastHttp = CineastHttp(cineastConfig)

    override val route: String = "/generate/room/random"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Generates a new room of random exhibits with the specified parameters.",
        path = "/api/generate/room/random",
        tags = ["Generation"],
        requestBody = OpenApiRequestBody(
            content = [OpenApiContent(RandomGenerationRequest::class)],
            required = true,
            description = "The generation configuration object as JSON string."
        ),
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(Room::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): Room {
        val config = ctx.bodyAsClass<RandomGenerationRequest>()

        return RandomRoomGenerator(config, cineastHttp).genRoom()
    }

}
