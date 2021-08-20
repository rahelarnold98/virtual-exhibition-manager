package ch.unibas.dmi.dbis.vrem.generation.som

import ch.unibas.dmi.dbis.som.PredictionResult
import ch.unibas.dmi.dbis.som.SOM
import ch.unibas.dmi.dbis.som.grids.Grid2DSquare
import ch.unibas.dmi.dbis.som.util.DistanceFunction
import ch.unibas.dmi.dbis.som.util.DistanceScalingFunction
import ch.unibas.dmi.dbis.som.util.TimeFunction
import ch.unibas.dmi.dbis.vrem.generation.CineastHttp
import ch.unibas.dmi.dbis.vrem.generation.CineastRest
import ch.unibas.dmi.dbis.vrem.generation.Generator
import ch.unibas.dmi.dbis.vrem.generation.model.GenerationConfig
import ch.unibas.dmi.dbis.vrem.generation.model.IdDoublePair
import ch.unibas.dmi.dbis.vrem.generation.model.MetadataType
import ch.unibas.dmi.dbis.vrem.generation.model.NodeMap
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibition
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class SomGenerator(
    genConfig: GenerationConfig,
    cineastHttp: CineastHttp
) : Generator(genConfig, cineastHttp) {

    companion object {
        const val EX_TEXT = "Generated Exhibition (SOM)"
        const val ROOM_TEXT = "Generated Room (SOM)"
    }

    private fun trainSom(features: Array<DoubleArray>): SOM {
        val height = genConfig.height
        val width = genConfig.width
        val epochs = 100 // TODO Calculate this dynamically?
        val seed = Random(genConfig.seed)

        val g = Grid2DSquare(
            height,
            width,
            featureDepth = features[0].size,
            distanceFunction = DistanceFunction.euclideanNorm2DTorus(
                intArrayOf(height, width),
                booleanArrayOf(false, true) // Wrap around width, but do not wrap around height.
            ),
            rand = seed
        )

        val s = SOM(
            g,
            distanceScaling = DistanceScalingFunction.exponentialDecreasing(),
            alpha = TimeFunction.linearDecreasingFactorScaled(1.0),
            sigma = TimeFunction.defaultSigmaFunction(intArrayOf(height, width), 2.0),
            rand = seed
        )

        s.train(features, epochs)

        return s
    }

    private fun predictionsToNodeMap(
        numNodes: Int,
        predictions: ArrayList<PredictionResult>,
        ids: ArrayList<String>
    ): NodeMap {
        val nodeMap = NodeMap()

        // Add all nodes to the map.
        for (i in 0 until numNodes) {
            nodeMap.addEmptyNode(i)
        }

        // Add every classified sample to the corresponding node.
        for (i in ids.indices) {
            nodeMap.addClassifiedSample(ids[i], predictions[i])
        }

        // Sort lists.
        for ((k, v) in nodeMap.map.entries) {
            // Sort ascending which is what we want (smaller distance = more similar).
            nodeMap.map[k] = v.sortedWith(compareBy(IdDoublePair::value)).toCollection(ArrayList())
        }

        return nodeMap
    }

    private fun createWallsFromNodeMap(dims: IntArray, nodeMap: NodeMap): MutableList<Wall> {
        val idList = mutableListOf<String?>()

        // Pick top image for every node and add it.
        for (idDistanceList in nodeMap.map.values) { // Linked hash map, ordered according to node ID.
            if (idDistanceList.isEmpty()) {
                idList.add(null)
            } else {
                idList.add(idDistanceList[0].id)
            }
        }

        /*
         * The height dimension of the SOM grid is inverted here but that doesn't matter for the visualization
         * since we do not have any preset weights for specific SOM nodes when generating the SOM.
         */
        return exhibitListToWalls(dims, idListToExhibits(idList))
    }

    override fun genRoom(): Room {
        // Get all features for category.
        val allFeatures = CineastRest.getFeatureDataFromCategory(genConfig.genType.cineastCategory)

        val room = Room(text = ROOM_TEXT + " " + getTextSuffix())

        // Pick the feature type we actually want.
        val features = allFeatures[genConfig.genType.tableName] ?: return room

        // Remove IDs if we're filtering by ID list.
        // TODO If this is too expensive, create a new Cineast API call to obtain features for certain IDs only.
        features.filterByList(genConfig.idList)

        // Get all values as 2D array.
        val data = features.valuesTo2DArray() // Same order as the IDs.
        val ids = features.getSortedIds()

        // TODO Normalize data (e.g., for color features).

        // Train SOM.
        val som = trainSom(data)

        // Predict data.
        val predictions = som.predict(data)

        // Create node map.
        val nodeMap = predictionsToNodeMap(som.grid.nodes.size, predictions, ids)

        // Create exhibitions depending on settings (we could create the sub-rooms right away as well).
        val walls = createWallsFromNodeMap(som.grid.dims, nodeMap)

        // Set room size and add walls.
        room.size = roomSizeFromWalls(walls)
        room.walls.addAll(walls)

        // Encode node map to JSON to add as metadata.
        room.metadata[MetadataType.SOM_IDS.key] = Json.encodeToString(NodeMap.serializer(), nodeMap)

        // Add seed information to room.
        room.metadata[MetadataType.SEED.key] = genConfig.seed.toString()

        return room
    }

    override fun genExhibition(): Exhibition {
        val room = genRoom()

        val ex = Exhibition(name = EX_TEXT + " " + getTextSuffix())

        ex.rooms.add(room)

        printAnglesForEx(ex)

        return ex
    }

}
