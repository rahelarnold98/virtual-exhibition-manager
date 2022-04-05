package ch.unibas.dmi.dbis.vrem.rest.responses

import ch.unibas.dmi.dbis.vrem.model.exhibition.ExhibitionSummary
import kotlinx.serialization.Serializable

/**
 * Response for the action to list exhibitions (effectively wrapping them).
 *
 * @property exhibitions The list of exhibitions.
 */
@Serializable
data class ListExhibitionsResponse(val exhibitions: List<ExhibitionSummary>)
