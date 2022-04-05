package ch.unibas.dmi.dbis.vrem.config

import com.mongodb.ConnectionString
import kotlinx.serialization.Serializable

/**
 * Configuration of the MongoDB database instance.
 *
 * @property host The host address of the MongoDB instance.
 * @property port The port of the MongoDB instance.
 * @property database The name of the database.
 */
@Serializable
data class DatabaseConfig(
    val host: String,
    val port: Short,
    val database: String
) {

    /**
     * Returns the connection string for the MongoDB client.
     *
     * @return The connection string based on host and port.
     */
    fun getConnectionString(): ConnectionString {
        return ConnectionString("mongodb://$host:$port")
    }

}
