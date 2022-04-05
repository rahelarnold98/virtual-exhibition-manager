package ch.unibas.dmi.dbis.vrem.database

import ch.unibas.dmi.dbis.vrem.model.collection.ArtCollection
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.model.exhibition.ExhibitionSummary
import com.mongodb.client.MongoDatabase
import org.litote.kmongo.eq
import org.litote.kmongo.findOne
import org.litote.kmongo.getCollection
import org.litote.kmongo.projection

/**
 * MongoDB reader for VREM.
 *
 * @param database The MongoDatabase object to create the reader for.
 */
class VREMReader(database: MongoDatabase) : VREMDao(database) {

    /**
     * Checks whether an exhibition with a given name (string) exists in the exhibition collection.
     *
     * @param name The name of the exhibition.
     * @return True if the collection exists, false otherwise.
     */
    fun existsExhibition(name: String): Boolean {
        val col = getExhibitionCollection()
        return col.find(Exhibition::name eq name).any()
    }

    /**
     * Retrieves an exhibition from the exhibition collection by name.
     *
     * @param name The name of the exhibition.
     * @return The exhibition as an object.
     */
    fun getExhibitionByName(name: String): Exhibition {
        val col = getExhibitionCollection()
        return col.findOne(Exhibition::name eq name)!!
    }

    /**
     * Retrieves an exhibition from the exhibition collection by id.
     *
     * @param id The ID of the exhibition.
     * @return The exhibition as an object.
     */
    fun getExhibitionById(id: String): Exhibition {
        val col = getExhibitionCollection()
        return col.findOne { Exhibition::id eq id }!!
    }

    /**
     * Obtains a list of exhibitions and their respective ID.
     *
     * @return The generated list.
     */
    fun listExhibitions(): List<ExhibitionSummary> {
        val col = getExhibitionCollection()
        return col.find().projection(Exhibition::id, Exhibition::name)
            .map { ExhibitionSummary(it.id.toString(), it.name) }.toMutableList()
    }

    /**
     * Lists all exhibits in the corpus collection.
     *
     * @return A list of exhibits.
     */
    fun listExhibits(): List<Exhibit> {
        val artCollections = database.getCollection<ArtCollection>(CORPUS_COLLECTION)
        return artCollections.find().first()?.exhibits!!
    }

    /**
     * List all exhibits that are currently part of any exhibition.
     *
     * @return The list of all exhibits in the exhibitions.
     */
    fun listExhibitsFromExhibitions(): List<Exhibition> {
        val exhibitions = database.getCollection(EXHIBITION_COLLECTION, Exhibition::class.java)
        return exhibitions.find().toMutableList()
    }

}
