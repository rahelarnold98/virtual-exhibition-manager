package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

@Serializable
class FeatureWeightPair(val tableName: String, val weight: Double)

/**
 * Configuration for SOM features to use.
 *
 * @property featureMap A mapping of the generation type, provided by VREP to the table name of the features to obtain as well as their weight.
 */
@Serializable
class SomFeatureConfig(val featureMap: Map<String, List<FeatureWeightPair>>)
