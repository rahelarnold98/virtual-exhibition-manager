package ch.unibas.dmi.dbis.vrem.generation.generators

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.model.exhibition.MetadataType
import java.time.LocalDateTime

/**
 * Generic exhibition generator.
 */
open class ExhibitionGenerator {

    /**
     * Returns the current time to use as suffix for the name of the object.
     *
     * @return The current time.
     */
    fun getTextSuffix(): LocalDateTime? = LocalDateTime.now()

    private val exhibitionText: String = "Generated Exhibition"

    /**
     * Generates an empty exhibition object.
     *
     * @param name The name of the exhibition.
     * @return The newly created exhibition with the generated metadata attribute set to true.
     */
    open fun genExhibition(name: String = exhibitionText + " " + getTextSuffix()): Exhibition {
        // Generic exhibition object without rooms or anything.
        val ex = Exhibition(name = name)
        ex.metadata[MetadataType.GENERATED.key] = true.toString()

        return ex
    }

}
