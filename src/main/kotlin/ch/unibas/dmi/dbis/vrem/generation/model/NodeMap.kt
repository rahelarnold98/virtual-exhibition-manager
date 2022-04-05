package ch.unibas.dmi.dbis.vrem.generation.model

import ch.unibas.dmi.dbis.som.PredictionResult
import kotlinx.serialization.Serializable

@Serializable
class NodeMap {

    val map = LinkedHashMap<Int, MutableList<IdDoublePair>>()

    fun addEmptyNode(i: Int) {
        map[i] = ArrayList()
    }

    fun addClassifiedSample(sampleId: String, res: PredictionResult) {
        if (map[res.nodeId] == null) {
            addEmptyNode(res.nodeId)
        }

        map[res.nodeId]!!.add(IdDoublePair(sampleId, res.distance))
    }

}
