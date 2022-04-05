package ch.unibas.dmi.dbis.vrem.rest.responses

import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import kotlinx.serialization.Serializable

/**
 * Response for the action to list exhibits (effectively wrapping them).
 *
 * @property exhibits The list of exhibits.
 */
@Serializable
data class ListExhibitsResponse(val exhibits: List<Exhibit>)
