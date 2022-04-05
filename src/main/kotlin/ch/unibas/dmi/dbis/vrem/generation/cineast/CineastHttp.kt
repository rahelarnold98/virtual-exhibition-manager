package ch.unibas.dmi.dbis.vrem.generation.cineast

import ch.unibas.dmi.dbis.vrem.config.CineastConfig
import com.github.kittinunf.fuel.httpGet

/**
 * Cineast HTTP request object to obtain image files (as we cannot use the OpenAPI client to obtain this yet...).
 *
 * @property cineastConfig A configuration for the Cineast instance.
 */
class CineastHttp(private val cineastConfig: CineastConfig) {

    /**
     * Requests an object by ID (needs to have the segment suffix).
     *
     * @param id The ID of the object.
     * @return A byte array of the object.
     */
    fun objectRequest(id: String): ByteArray {
        val (_, _, result) = cineastConfig.getCineastObjectUrlString(id)
            .httpGet()
            .timeout(cineastConfig.queryTimeoutSeconds.toInt())
            .response()

        return result.get()
    }

}
