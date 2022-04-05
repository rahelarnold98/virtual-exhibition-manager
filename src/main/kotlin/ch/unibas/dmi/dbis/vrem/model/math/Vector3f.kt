package ch.unibas.dmi.dbis.vrem.model.math

import com.fasterxml.jackson.annotation.JsonIgnore
import kotlinx.serialization.Serializable

/**
 * 3D float vector.
 *
 * @property x x value as float.
 * @property y y value as float.
 * @property z z value as float.
 */
@Serializable
data class Vector3f(val x: Float = 0f, val y: Float = 0f, val z: Float = 0f) {

    constructor(x: Number, y: Number, z: Number) : this(x.toFloat(), y.toFloat(), z.toFloat())

    constructor(x: Number, y: Number) : this(x.toFloat(), y.toFloat(), 0f)

    private constructor(x: Float) : this(x, x, x)

    companion object {
        val ORIGIN = Vector3f(0f)
        val UNIT = Vector3f(1f)
        val NaN = Vector3f(Float.NaN)
    }

    /**
     * Checks whether a vector is (NaN, NaN, NaN).
     *
     * @return True if the vector object is NaN, false otherwise.
     */
    @JsonIgnore // Due to OpenAPI basically requiring Jackson.
    fun isNaN(): Boolean {
        return equals(NaN)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Vector3f

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x.hashCode()
        result = 31 * result + y.hashCode()
        result = 31 * result + z.hashCode()
        return result
    }

    override fun toString(): String {
        return "Vector3f($x, $y, $z)"
    }

}
