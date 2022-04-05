package ch.unibas.dmi.dbis.vrem.model.exhibition

/**
 * Enumeration of the compass directions for walls and the axis the wall is running parallel against.
 *
 * @param axis The axis the wall is running parallel against.
 */
enum class Direction(val axis: Coordinate) {

    NORTH(Coordinate.X),
    EAST(Coordinate.Z),
    SOUTH(Coordinate.X),
    WEST(Coordinate.Z);

    enum class Coordinate {

        X,
        Y,
        Z

    }

}
