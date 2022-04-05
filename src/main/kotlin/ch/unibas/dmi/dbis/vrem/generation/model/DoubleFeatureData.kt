package ch.unibas.dmi.dbis.vrem.generation.model

import kotlin.math.max
import kotlin.math.sqrt

data class DoubleFeatureData(val featureName: String) {

    private val idMap: MutableMap<String, ArrayList<Double>> = mutableMapOf()

    companion object {

        const val CONCAT_NAME = "concat"

        fun concatenate(data: ArrayList<DoubleFeatureData>): DoubleFeatureData {
            val dfd = DoubleFeatureData(CONCAT_NAME)

            if (data.isEmpty()) {
                return dfd
            }

            val ids = data[0].getSortedIds()

            for (id in ids) {
                val concatList = ArrayList<Double>()

                for (feature in data) {
                    concatList.addAll(feature.idMap[id]!!)
                }

                dfd.addSample(id, concatList)
            }

            return dfd
        }

    }

    fun numSamples(): Int = idMap.size

    fun addSample(id: String, value: ArrayList<Double>) {
        idMap[id] = value
    }

    fun getSortedIds(): ArrayList<String> {
        return idMap.keys.sorted().toCollection(ArrayList())
    }

    fun normalize(scale: Double = 1.0): DoubleFeatureData {
        // Calculate maximum distance.
        var maxDist = 0.0

        for (arr: ArrayList<Double> in idMap.values) {
            var currDist = 0.0

            // This should be faster than map/reduce.
            for (value in arr) {
                currDist += value * value
            }

            maxDist = max(maxDist, currDist)
        }

        maxDist = sqrt(maxDist)

        // Scale everything.
        for (arr in idMap.values) {
            for (i in arr.indices) {
                arr[i] = scale * (arr[i] / maxDist)
            }
        }

        return this
    }

    fun valuesTo2DArray(): Array<DoubleArray> {
        val arr = Array(idMap.size) { doubleArrayOf() }
        val keys = idMap.keys.sorted() // Make sure we have the keys sorted, this simplifies feature concatenation.
        var currIdx = 0

        for (k in keys) {
            arr[currIdx++] = idMap[k]!!.toDoubleArray()
        }

        return arr
    }

    fun filterByList(ids: ArrayList<String>) {
        if (ids.isNotEmpty()) {
            idMap.keys.retainAll(ids)
        }
    }

}
