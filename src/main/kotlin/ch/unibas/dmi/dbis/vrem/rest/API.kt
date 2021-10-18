package ch.unibas.dmi.dbis.vrem.rest

import ch.unibas.dmi.dbis.vrem.cineast.client.infrastructure.ApiClient
import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.database.VREMDao
import ch.unibas.dmi.dbis.vrem.database.VREMReader
import ch.unibas.dmi.dbis.vrem.database.VREMWriter
import ch.unibas.dmi.dbis.vrem.rest.handlers.DeleteRestHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.GetRestHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.PostRestHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.PutRestHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.content.PathContentHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.content.QueryContentHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibit.ListExhibitsHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibit.SaveExhibitHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition.ListExhibitionsHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition.LoadExhibitionByIdHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition.LoadExhibitionByNameHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.exhibition.SaveExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.generation.exhibition.EmptyExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.generation.room.RandomRoomHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.generation.room.SimilarityRoomHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.generation.room.SomRoomHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JsonMapper
import io.javalin.plugin.openapi.OpenApiOptions
import io.javalin.plugin.openapi.OpenApiPlugin
import io.javalin.plugin.openapi.jackson.JacksonToJsonMapper
import io.javalin.plugin.openapi.jackson.ToJsonMapper
import io.javalin.plugin.openapi.ui.SwaggerOptions
import io.swagger.v3.oas.models.info.Info
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import mu.KotlinLogging
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory
import org.eclipse.jetty.server.*
import org.eclipse.jetty.util.ssl.SslContextFactory
import org.eclipse.jetty.util.thread.QueuedThreadPool
import java.io.File
import java.time.Duration
import kotlin.io.path.exists

private val logger = KotlinLogging.logger {}

/**
 * VREM API endpoint class.
 */
@ExperimentalSerializationApi
class API : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    private val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    /**
     * Serializer for OpenAPI; unfortunately requires Jackson (or a custom implementation of kotlinx since we cannot
     * annotate the required OpenAPI classes).
     */
    private val openApiSerializer = object : ToJsonMapper {
        override fun map(obj: Any): String {
            return JacksonToJsonMapper(JacksonToJsonMapper.defaultObjectMapper).map(obj)
        }
    }

    companion object {
        /**
         * Replaces the Jackson mapper with a kotlinx serialization mapper.
         */
        val jsonMapper = object : JsonMapper {
            override fun toJsonString(obj: Any): String {
                val serializer = serializer(obj.javaClass)
                val jsonObj = Json {
                    // serializersModule = IdKotlinXSerializationModule // To properly serialize IDs.
                    encodeDefaults = true // Don't omit values generated by default.
                }
                return jsonObj.encodeToString(serializer, obj)
            }

            override fun <T : Any?> fromJsonString(json: String, targetClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                val deserializer = serializer(targetClass) as KSerializer<T>
                val jsonObj = Json {
                    // serializersModule = IdKotlinXSerializationModule // To properly deserialize IDs.
                    coerceInputValues = true // Use default values if key not provided.
                    ignoreUnknownKeys = true // Just ignore unknown keys and use what we have.
                }
                return jsonObj.decodeFromString(deserializer, json)
            }
        }
    }

    override fun run() {
        val config = Config.readConfig(this.config)
        val docRoot = File(config.server.documentRoot).toPath()

        val pair = VREMDao.getDAOs(config.database)
        val reader: VREMReader = pair.first
        val writer: VREMWriter = pair.second

        // Give Cineast enough time to process the request before timing out.
        System.getProperties().setProperty(
            "ch.unibas.dmi.dbis.vrem.cineast.client.baseUrl",
            "${config.cineast.host}:${config.cineast.port}"
        )
        ApiClient.builder.readTimeout(Duration.ofSeconds(config.cineast.queryTimeoutSeconds))

        // Handlers.
        val apiRestHandlers = listOf(
            QueryContentHandler(docRoot, config.cineast),
            PathContentHandler(docRoot, config.cineast),
            ListExhibitsHandler(reader),
            SaveExhibitHandler(writer, docRoot),
            ListExhibitionsHandler(reader),
            LoadExhibitionByIdHandler(reader),
            LoadExhibitionByNameHandler(reader),
            SaveExhibitionHandler(writer),
            EmptyExhibitionHandler(),
            RandomRoomHandler(config.cineast),
            SimilarityRoomHandler(config.cineast),
            SomRoomHandler(config.cineast),
        )

        // API endpoint.
        val endpoint = Javalin.create { conf ->
            conf.registerPlugin(
                OpenApiPlugin(
                    OpenApiOptions(
                        Info().apply {
                            version("1.0")
                            description("Virtual Exhibition Manager API")
                        }
                    ).apply {
                        path("/swagger-docs")
                        swagger(SwaggerOptions("/swagger-ui"))
                        activateAnnotationScanningFor("ch.unibas.dmi.dbis.vrem")
                        toJsonMapper(openApiSerializer) // Set serializer.
                    }
                )
            )

            // Only add document root if it exists.
            if (docRoot.exists()) {
                conf.addStaticFiles(config.server.documentRoot, Location.EXTERNAL)
            }

            // Various config stuff.
            conf.server { setupServer(config) }
            conf.enforceSsl = config.server.enableSsl
            conf.defaultContentType = "application/json"
            conf.enableCorsForAllOrigins()
            conf.jsonMapper(jsonMapper)

            // Logger.
            conf.requestLogger { ctx, ms ->
                logger.info { "Request received: ${ctx.req.requestURI}" }
            }
        }.routes {
            path("api") {
                apiRestHandlers.forEach { handler ->

                    path(handler.route) {

                        if (handler is GetRestHandler<*>) {
                            get(handler::get)
                        }

                        if (handler is PostRestHandler<*>) {
                            post(handler::post)
                        }

                        if (handler is PutRestHandler<*>) {
                            put(handler::put)
                        }

                        if (handler is DeleteRestHandler<*>) {
                            delete(handler::delete)
                        }

                    }

                }
            }
        }

        endpoint.after { ctx ->
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")
        }

        endpoint.start(config.server.httpPort)

        println("Started the server.")
        println("Ctrl+C to stop the server.")
    }

}

/**
 * Setup for the server for HTTP(S).
 *
 * @param config A configuration object.
 * @return The created server instance.
 */
private fun setupServer(config: Config): Server {
    val threadPool = QueuedThreadPool()
    threadPool.name = "server"

    val httpConfig = HttpConfiguration().apply {
        sendServerVersion = false
        sendXPoweredBy = false

        if (config.server.enableSsl) {
            secureScheme = "https"
            securePort = config.server.httpsPort
        }
    }

    // https://www.eclipse.org/jetty/documentation/jetty-11/programming_guide.php
    // Section "encrypted http/2".
    if (config.server.enableSsl) {
        // SSL.
        val httpsConfig = HttpConfiguration(httpConfig).apply {
            addCustomizer(SecureRequestCustomizer())
        }

        val fallback = HttpConnectionFactory(httpsConfig)

        val alpn = ALPNServerConnectionFactory().apply {
            defaultProtocol = fallback.protocol
        }

        val sslContextFactory = SslContextFactory.Server().apply {
            keyStorePath = config.server.keystorePath
            setKeyStorePassword(config.server.keystorePass)
            // cipherComparator = HTTP2Cipher.COMPARATOR
            provider = "Conscrypt"
        }

        val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)
        val http2 = HTTP2ServerConnectionFactory(httpsConfig)

        return Server(threadPool).apply {
            // HTTP Connector.
            addConnector(
                ServerConnector(
                    server,
                    HttpConnectionFactory(httpConfig),
                    HTTP2ServerConnectionFactory(httpConfig)
                ).apply {
                    port = config.server.httpPort
                }
            )

            // HTTPS Connector.
            addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply {
                port = config.server.httpsPort
            })
        }
    } else {
        // No SSL.
        return Server(threadPool).apply {
            // HTTP Connector.
            addConnector(
                ServerConnector(
                    server,
                    HttpConnectionFactory(httpConfig),
                    HTTP2ServerConnectionFactory(httpConfig)
                ).apply {
                    port = config.server.httpPort
                }
            )
        }
    }
}
