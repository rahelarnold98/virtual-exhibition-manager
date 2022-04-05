package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

/**
 * VREM configuration with database and webserver, deserialized form of config.json-like files.
 *
 * @property database The MongoDB configuration object.
 * @property server The web server configuration object (i.e., VREM document root and port).
 * @property cineast The Cineast instance configuration object.
 */
@Serializable
data class Config(
    val database: DatabaseConfig,
    val server: WebServerConfig,
    val cineast: CineastConfig,
) {

    companion object {

        const val DEFAULT_CONFIG_FILE = "config.json"

        /**
         * Parses the specified configuration file for the exhibition.
         *
         * @param config The name of the JSON configuration file (relative to the cwd).
         * @return The parsed configuration file.
         */
        fun readConfig(config: String = DEFAULT_CONFIG_FILE): Config {
            val jsonString = File(config).readText()
            return Json.decodeFromString(serializer(), jsonString)
        }

    }

}
