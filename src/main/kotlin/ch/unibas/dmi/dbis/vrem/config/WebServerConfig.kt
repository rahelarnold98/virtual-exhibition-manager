package ch.unibas.dmi.dbis.vrem.config

import kotlinx.serialization.Serializable

/**
 * Configuration of the REST endpoint.
 *
 * @property documentRoot The document root path as a string.
 * @property httpPort The HTTP port of the endpoint.
 * @property httpsPort The HTTPS port of the endpoint.
 * @property enableSsl Whether to enable SSL (HTTPS) or not.
 * @property keystorePath The path to find the keystore for SSL on.
 * @property keystorePassword The password for the keystore.
 */
@Serializable
data class WebServerConfig(
    val documentRoot: String,
    val httpPort: Int,
    val httpsPort: Int,
    val enableSsl: Boolean,
    val keystorePath: String,
    val keystorePassword: String
)
