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
 * @param type
 * @param keywords
 */

interface AbstractMetadataFilterDescriptor {

    @Json(name = "type")
    val type: kotlin.String

    @Json(name = "keywords")
    val keywords: kotlin.collections.List<kotlin.String>?
}

