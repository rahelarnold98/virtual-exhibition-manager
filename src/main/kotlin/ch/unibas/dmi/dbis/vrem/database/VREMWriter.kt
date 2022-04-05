package ch.unibas.dmi.dbis.vrem.database

import ch.unibas.dmi.dbis.vrem.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.rest.requests.ExhibitUploadRequest
import com.mongodb.client.MongoDatabase
import mu.KotlinLogging
import org.litote.kmongo.eq
import org.litote.kmongo.push
import org.litote.kmongo.replaceOneById

private val logger = KotlinLogging.logger {}

/**
 * MongoDB writer for VREM.
 *
 * @param database The MongoDB object to create the writer for.
 */
class VREMWriter(database: MongoDatabase) : VREMDao(database) {

    /**
     * Stores an exhibition in the exhibition collection.
     *
     * @param exhibition The exhibition to store.
     * @return True if any changes were made, false otherwise.
     */
    fun saveExhibition(exhibition: Exhibition): Boolean {
        val collection = getExhibitionCollection()
        val result = collection.replaceOneById(exhibition.id, exhibition)
        if (result.matchedCount == 0L) {
            collection.insertOne(exhibition)
        }
        return result.modifiedCount != 0L
    }

    /**
     * Store a single exhibit in the exhibit corpus.
     * Creates the collection if it doesn't exist.
     *
     * @param exhibitUploadRequest A request for the object to upload.
     * @return The path of the uploaded exhibit.
     */
    fun uploadExhibit(exhibitUploadRequest: ExhibitUploadRequest): String {
        val collection = getCorporaCollection()

        val toAdd = Exhibit.copy(exhibitUploadRequest.exhibit)
        toAdd.path = "${exhibitUploadRequest.artCollection}/${toAdd.id}.${exhibitUploadRequest.fileExtension}"

        if (collection.countDocuments() == 0L) {
            logger.debug { "There is no previous document in $CORPUS_COLLECTION, creating default corpus document." }

            val artCollection = ArtCollection(name = "DefaultCorpus", exhibits = listOf(toAdd))
            collection.insertOne(artCollection)
            logger.info { "Successfully created the default corpus 'DefaultCorpus' and added the exhibit to it." }
        } else {
            val result = collection.updateOne(
                ArtCollection::name eq exhibitUploadRequest.artCollection,
                push(ArtCollection::exhibits, toAdd)
            )

            if (result.matchedCount == 0L) {
                logger.error { "Could not update corpus ${exhibitUploadRequest.artCollection}." }
            } else {
                logger.info { "Updated corpus ${exhibitUploadRequest.artCollection}." }
            }
        }
        return toAdd.path
    }

    /**
     * Removes an exhibition from the collection by name.
     *
     * @param name The name of the exhibition to remove.
     */
    fun deleteExhibition(name: String) {
        getExhibitionCollection().deleteMany(Exhibition::name eq name)
    }

}
