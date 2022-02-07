package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.database.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.api.request.ExhibitUploadRequest
import ch.unibas.dmi.dbis.vrem.model.api.response.ListExhibitsResponse
import io.javalin.http.Context
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import java.util.*

private val logger = KotlinLogging.logger {}

/**
 *
 *
 * @property reader The VREM MongoDB reader object.
 * @property writer The VREM MongoDB writer object.
 * @property docRoot The root folder of the exhibition. // TODO Fixme.
 */
class ExhibitHandler(private val reader: VREMReader, private val writer: VREMWriter, private val docRoot: Path) {

    /**
     * Lists all exhibits currently stored.
     *
     * @param ctx The Javalin request context.
     */
    fun listExhibits(ctx: Context) {
        logger.debug { "List exhibits request received." }
        // Serialize object to JSON.
        ctx.json(ListExhibitsResponse(reader.listExhibits()))

        // TODO What to do here?
    }

    /**
     * Stores an exhibit in the MongoDB and filesystem.
     *
     * @param ctx The Javalin request context.
     */
    fun saveExhibit(ctx: Context) {
        logger.debug { "Save exhibit request received." }

        // Map the JSON body to a Kotlin object.
        val exhibitUpload = ctx.bodyAsClass<ExhibitUploadRequest>()

        logger.debug { "Save exhibit.id=${exhibitUpload.exhibit.id} and for corpus ${exhibitUpload.artCollection}." }
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
        // return exhibitUpload
        // TODO What to return here?
    }

}
