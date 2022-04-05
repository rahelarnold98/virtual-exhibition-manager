package ch.unibas.dmi.dbis.vrem.generation.generators

import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastClient
import ch.unibas.dmi.dbis.vrem.generation.cineast.CineastHttp
import ch.unibas.dmi.dbis.vrem.model.exhibition.MetadataType
import ch.unibas.dmi.dbis.vrem.model.exhibition.Room
import ch.unibas.dmi.dbis.vrem.rest.requests.RandomGenerationRequest
import kotlin.math.min
import kotlin.random.Random

/**
 * Random room generator.
 *
 * @property genConfig The request issued for the room generation.
 * @property cineastHttp Cineast HTTP client to obtain data.
 */
class RandomRoomGenerator(
    private val genConfig: RandomGenerationRequest,
    cineastHttp: CineastHttp
) : RoomGenerator(cineastHttp) {

    override val roomText = "Generated Room (Random)"

    override fun genRoom(): Room {
        var ids = if (genConfig.idList.isEmpty()) {
            CineastClient.getAllIds()
        } else {
            genConfig.idList
        }.shuffled(Random(genConfig.seed))

        ids = ids.subList(0, min(genConfig.roomSpec.height * genConfig.roomSpec.width, ids.size))

        val exhibits = idListToExhibits(ids)

        val walls = exhibitListToWalls(intArrayOf(genConfig.roomSpec.height, genConfig.roomSpec.width), exhibits)

        val room = wallsToRoom(walls)
        room.metadata[MetadataType.SEED.key] = genConfig.seed.toString()

        return room
    }

}
