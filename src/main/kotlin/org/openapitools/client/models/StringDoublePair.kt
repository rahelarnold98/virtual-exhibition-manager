/**
 * Cineast RESTful API
 * Cineast is vitrivr's content-based multimedia retrieval engine. This is it's RESTful API.
 *
 * The version of the OpenAPI document: v1
 * Contact: contact@vitrivr.org
 *
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */
package org.openapitools.client.models


import com.squareup.moshi.Json

/**
 *
 * @param key
 * @param value
 */

data class StringDoublePair(
    @Json(name = "key")
    val key: kotlin.String? = null,
    @Json(name = "value")
    val value: kotlin.Double? = null
)

