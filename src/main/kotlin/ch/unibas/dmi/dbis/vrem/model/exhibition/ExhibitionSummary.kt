package ch.unibas.dmi.dbis.vrem.model.exhibition

import kotlinx.serialization.Serializable

/**
 * Exhibition summary in the form of a tuple of object ID and the name of the exhibition.
 *
 * @property id The ID of the exhibition.
 * @property name The name of the exhibition.
 */
@Serializable
data class ExhibitionSummary(
    val id: String,
    val name: String
)
