package ch.unibas.dmi.dbis.vrem.generation.generators

import ch.unibas.dmi.dbis.vrem.cineast.client.apis.SegmentsApi
import ch.unibas.dmi.dbis.vrem.cineast.client.models.*
import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastHttp
import ch.unibas.dmi.dbis.vrem.model.exhibition.Exhibit
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.model.exhibition.Wall
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import ch.unibas.dmi.dbis.vrem.rest.requests.SimilarityGenerationRequest
import kotlin.math.min

/**
 * Similarity room generator.
 *
 * @property genConfig The request issued for the room generation.
 * @property category The category to use for the similarity search in Cineast.
 * @property cineastHttp Cineast HTTP client to obtain data.
 */
class SimilarityRoomGenerator(
    private val genConfig: SimilarityGenerationRequest,
    private val category: String,
    cineastHttp: CineastHttp
) : RoomGenerator(cineastHttp) {

    override val roomText: String = "Generated Room (Similarity)"

    /**
     * Puts the first exhibit on the north wall and splits the remaining ones across the other walls.
     *
     * @param exhibits The exhibits to put
     * @return A list of the four walls with their exhibits.
     */
    private fun similarExhibitsToWalls(
        exhibits: MutableList<Exhibit>
    ): MutableList<Wall> {
        val walls = wallEnumToList()

        if (exhibits.isEmpty()) {
            return walls
        }

        // Remove first exhibit with the highest similarity.
        val queryExhibit = exhibits.removeFirst()

        // Add exhibit separately to north wall.
        walls[0].exhibits.add(queryExhibit)

        if (exhibits.isEmpty()) {
            // Set query exhibit position (just place it, it's the only one).
            queryExhibit.position = getWallPositionByCoords(0, 0)

            return walls
        }

        // Place remaining exhibits on the 3 remaining walls.
        for (i in 0 until genConfig.roomSpec.height) {
            for (w in 1 until walls.size) { // Ignore north wall!
                for (j in 0 until genConfig.roomSpec.width / walls.size) {
                    if (exhibits.isEmpty()) {
                        break
                    }

                    val exhibit = exhibits.removeFirst()

                    exhibit.position = getWallPositionByCoords(i, j)

                    walls[w].exhibits.add(exhibit)
                }
            }
        }

        val queryExhibitX = 0.5 * getRoomDimFromWalls(walls).x
        val queryExhibitY = getWallPositionByCoords(0, 0).y
        queryExhibit.position = Vector3f(queryExhibitX, queryExhibitY, 0.0f)

        return walls
    }

    override fun genRoom(): Room {
        val containers = mutableListOf<QueryComponent>()
        containers.add(
            QueryComponent(
                mutableListOf(
                    QueryTerm(
                        QueryTerm.Type.ID,
                        genConfig.objectId,
                        mutableListOf(category)
                    )
                )
            )
        )

        val query = SimilarityQuery(containers) // Omit messageType or the query will fail.

        val apiClient = SegmentsApi()
        val request = apiClient.findSegmentSimilar(query)
        val res: SimilarityQueryResult? = request.results?.find { it.category == category }

        val numExhibits = (0.75 * (genConfig.roomSpec.height * genConfig.roomSpec.width) + 1).toInt()

        var content: List<StringDoublePair> = res?.content!! // Contains key and value.
        content = content.subList(0, min(numExhibits, content.size))

        val ids = content.map { it.key!! }.toList()

        val exhibits = idListToExhibits(ids)

        val walls = similarExhibitsToWalls(exhibits)

        return wallsToRoom(walls)
    }

}
