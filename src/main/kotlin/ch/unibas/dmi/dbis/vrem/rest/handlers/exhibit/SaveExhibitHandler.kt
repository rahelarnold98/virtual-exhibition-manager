package ch.unibas.dmi.dbis.vrem.rest.handlers.exhibit

import ch.unibas.dmi.dbis.vrem.database.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.requests.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.rest.responses.ResponseMessage
import ch.unibas.dmi.dbis.vrem.rest.status.StatusCode
import io.javalin.http.Context
import io.javalin.plugin.openapi.annotations.*
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val logger = KotlinLogging.logger {}

class SaveExhibitHandler(private val writer: VREMWriter, private val docRoot: Path) : PostRestHandler<ResponseMessage> {

    override val route: String
        get() = "/exhibits/save"

    @OpenApi(
        method = HttpMethod.POST,
        summary = "Stores an exhibit to the database.",
        path = "/api/exhibits/save",
        tags = ["Exhibit"],
        requestBody = OpenApiRequestBody(
            content = [OpenApiContent(Exhibit::class)],
            required = true,
            description = "An exhibit object as JSON string."
        ),
        responses = [
            OpenApiResponse(StatusCode.OK.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.FORBIDDEN.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.BAD_REQUEST.toString(), [OpenApiContent(ResponseMessage::class)]),
            OpenApiResponse(StatusCode.INTERNAL_SERVER_ERROR.toString(), [OpenApiContent(ResponseMessage::class)]),
        ]
    )
    override fun doPost(ctx: Context): ResponseMessage {
        logger.debug { "Save exhibit request received." }

        // Map the JSON body to a Kotlin object.
        val exhibitUpload = ctx.bodyAsClass<ExhibitUploadRequest>()

        logger.debug { "Saving exhibit with ID ${exhibitUpload.exhibit.id} for corpus ${exhibitUpload.artCollection}." }

        val path = writer.uploadExhibit(exhibitUpload)

        val base64Img = exhibitUpload.file.split(",")[1]
        val decodedImg = Base64.getDecoder().decode(base64Img.toByteArray(Charsets.UTF_8))

        val dir = docRoot.resolve(exhibitUpload.artCollection)
        if (!Files.exists(dir)) {
            dir.toFile().mkdirs()
        }

        val contentFile = docRoot.resolve(path).toFile()
        contentFile.writeBytes(decodedImg)

        logger.debug { "Stored exhibit and content to db and fs (${exhibitUpload.exhibit.id} / $contentFile." }

        return ResponseMessage("Successfully stored image.")
    }

}
