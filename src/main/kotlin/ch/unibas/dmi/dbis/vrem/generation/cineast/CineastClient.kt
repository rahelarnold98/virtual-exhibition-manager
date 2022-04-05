package ch.unibas.dmi.dbis.vrem.generation.cineast

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.MetadataApi
import ch.unibas.dmi.dbis.vrem.cineast.client.apis.ObjectApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.FeaturesByCategoryQueryResult
import ch.unibas.dmi.dbis.vrem.cineast.client.models.FeaturesByEntityQueryResult
import ch.unibas.dmi.dbis.vrem.cineast.client.models.IdList
import ch.unibas.dmi.dbis.vrem.generation.model.DoubleFeatureData

/**
 * Cineast client to make the necessary calls to obtain features and objects.
 */
object CineastClient {

    const val SEGMENT_SUFFIX = "_1"
    const val CINEAST_FEATURE_LABEL = "feature"
    const val CINEAST_ID_LABEL = "id"

    /**
     * Cleans an ID for an image by removing the segment suffix.
     *
     * @param id The ID to clean.
     * @return The cleaned ID.
     */
    fun cleanId(id: String): String {
        return id.substringBeforeLast(SEGMENT_SUFFIX)
    }

    /**
     * Obtains all IDs that Cineast has features stored for.
     *
     * @return The obtained IDs as a list.
     */
    fun getAllIds(): List<String> {
        val ids = ObjectApi().findObjectsAll()

        if (ids.content == null) {
            return arrayListOf()
        }

        return ids.content.map { o -> o.objectId + SEGMENT_SUFFIX }.toCollection(ArrayList())
    }

    /**
     * Obtains features for a given category.
     *
     * @param category The category to obtain the features for.
     * @param ids The IDs to obtain the features for (uses all IDs if unspecified).
     * @return A mapping of the feature's name to a list with the values and their associated IDs.
     */
    fun getFeaturesByCategory(category: String, ids: List<String>): Map<String, List<Map<String, Any>>> {
        val idList = IdList(ids)

        val features: FeaturesByCategoryQueryResult = MetadataApi().findFeaturesByCategory(category, idList)

        if (features.featureMap == null) {
            return mapOf()
        }

        return features.featureMap
    }

    /**
     * Obtains features for a given table name.
     *
     * @param tableName The name of the table to obtain the features for.
     * @param ids The IDs to obtain the features for (uses all IDs if unspecified).
     * @return A list of the obtained feature values and their associated IDs.
     */
    fun getFeatureDataByTableName(tableName: String, ids: List<String>): List<Map<String, Any>> {
        val idList = IdList(ids)

        val feature: FeaturesByEntityQueryResult = MetadataApi().findFeaturesByEntity(tableName, idList)

        if (feature.featureMap == null) {
            return listOf()
        }

        return feature.featureMap
    }

    /**
     * Converts a list of features and their values to [DoubleFeatureData] for easier processing.
     *
     * @param featureName The name of the feature.
     * @param featureList The list of the feature values and their IDs.
     * @return An object holding all the provided data.
     */
    fun featureListToFeatureData(featureName: String, featureList: List<Map<String, Any>>): DoubleFeatureData {
        val featureData = DoubleFeatureData(featureName)

        for (e in featureList) {
            @Suppress("UNCHECKED_CAST")
            featureData.addSample(
                (e[CINEAST_ID_LABEL] as String),
                e[CINEAST_FEATURE_LABEL] as ArrayList<Double>
            )
        }

        return featureData
    }

    /**
     * Obtains features for a category and converts them to [DoubleFeatureData] objects.
     *
     * @param category The name of the category in Cineast to retrieve.
     * @param ids An ID list to filter for (all IDs are used if empty or unspecified).
     * @return A mapping of the feature names and their associated [DoubleFeatureData] object.
     */
    fun getFeatureDataFromCategory(category: String, ids: List<String>): MutableMap<String, DoubleFeatureData> {
        val allFeatures = getFeaturesByCategory(category, ids)

        val featureDataList = mutableMapOf<String, DoubleFeatureData>()

        for (e in allFeatures.entries) {
            featureDataList[e.key] = featureListToFeatureData(e.key, e.value)
        }

        return featureDataList
    }

    /**
     * Obtains feature data for a given table name from Cineast.
     *
     * @param tableName The name of the table.
     * @param ids An ID list to filter for (all IDs are used if empty or unspecified).
     * @return The obtained data wrapped in a [DoubleFeatureData] object.
     */
    fun getFeatureDataFromTableName(tableName: String, ids: List<String>): DoubleFeatureData {
        val data = getFeatureDataByTableName(tableName, ids)
        return featureListToFeatureData(tableName, data)
    }

}
