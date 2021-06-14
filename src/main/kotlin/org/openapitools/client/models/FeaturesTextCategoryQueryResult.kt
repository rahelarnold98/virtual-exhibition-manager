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
 * @param queryId
 * @param featureValues
 * @param category
 * @param elementID
 */

data class FeaturesTextCategoryQueryResult(
    @Json(name = "queryId")
    val queryId: kotlin.String? = null,
    @Json(name = "featureValues")
    val featureValues: kotlin.collections.List<kotlin.String>? = null,
    @Json(name = "category")
    val category: kotlin.String? = null,
    @Json(name = "elementID")
    val elementID: kotlin.String? = null
)

