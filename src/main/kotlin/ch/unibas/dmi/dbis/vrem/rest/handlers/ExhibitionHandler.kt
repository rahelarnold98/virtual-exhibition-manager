package ch.unibas.dmi.dbis.vrem.rest.handlers

import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.database.VREMWriter
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.model.api.response.ListExhibitionsResponse
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import io.javalin.http.Context
import mu.KotlinLogging
import org.bson.types.ObjectId
import org.litote.kmongo.id.toId

private val logger = KotlinLogging.logger {}

/**
 * Exhibition handler for API requests.
 *
 * @property reader The VREM Reader instance to use for MongoDB access.
 * @property writer The VREM Writer instance to use for MongoDB access.
 */
class ExhibitionHandler(private val reader: VREMReader, private val writer: VREMWriter) {

    companion object {
        const val PARAM_KEY_ID = ":id"
        const val PARAM_KEY_NAME = ":name"
    }

    /**
     * Lists all exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun listExhibitions(ctx: Context) {
        logger.debug { "List exhibitions." }
        ctx.json(ListExhibitionsResponse(reader.listExhibitions()))
    }

    /**
     * Loads exhibition by ID from the collection of exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun loadExhibitionById(ctx: Context) {
        val id = ctx.pathParam(PARAM_KEY_ID)
        logger.debug { "Load exhibition with ID $id." }
        ctx.json(reader.getExhibition(ObjectId(id).toId()))
    }

    /**
     * Loads exhibition by name from the collection of exhibitions.
     *
     * @param ctx The Javalin request context.
     */
    fun loadExhibitionByName(ctx: Context) {
        val name = ctx.pathParam(PARAM_KEY_NAME)
        logger.debug { "Load exhibition with name $name." }
        ctx.json(reader.getExhibition(name))
    }

    /**
     * Stores an exhibition in MongoDB.
     *
     * @param ctx The Javalin request context.
     */
    fun saveExhibition(ctx: Context) {
        logger.debug { "Save exhibition request." }
        val exhibition = ctx.bodyAsClass<Exhibition>()
        logger.debug { "Save exhibition with ID ${exhibition.id}." }
        if (writer.saveExhibition(exhibition)) {
            logger.debug { "Successfully saved exhibition with ID ${exhibition.id}." }
            ctx.json(exhibition)
        } else {
            logger.debug { "Could not save exhibition.id=${exhibition.id} for unknown reasons." }
            ctx.status(500).json(ErrorResponse("Could not save exhibition (ID: ${exhibition.id})."))
        }
    }

}
