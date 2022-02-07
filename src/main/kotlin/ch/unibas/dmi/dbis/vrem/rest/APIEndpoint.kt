package ch.unibas.dmi.dbis.vrem.rest

import ch.unibas.dmi.dbis.vrem.cineast.client.infrastructure.ApiClient
import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.database.VREMDao
import ch.unibas.dmi.dbis.vrem.model.api.response.ErrorResponse
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.ExhibitionHandler
import ch.unibas.dmi.dbis.vrem.rest.handlers.RequestContentHandler
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import io.javalin.Javalin
import io.javalin.apibuilder.ApiBuilder.*
import io.javalin.http.staticfiles.Location
import io.javalin.plugin.json.JsonMapper
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
import org.litote.kmongo.id.serialization.IdKotlinXSerializationModule
import java.io.File
import java.time.Duration


private val logger = KotlinLogging.logger {}

/**
 * VREM API endpoint class.
 */
@ExperimentalSerializationApi
class APIEndpoint : CliktCommand(name = "server", help = "Start the REST API endpoint") {

    private val config: String by option("-c", "--config", help = "Path to the config file").default("config.json")

    override fun run() {
        val config = Config.readConfig(this.config)
        val (reader, writer) = VREMDao.getDAOs(config.database)
        val docRoot = File(config.server.documentRoot).toPath()

        // Give Cineast enough time to process the request before timing out.
        ApiClient.builder.readTimeout(Duration.ofSeconds(config.cineast.queryTimeoutSeconds))

        // Handlers.
        val exhibitionHandler = ExhibitionHandler(reader, writer)
        val contentHandler = RequestContentHandler(docRoot, config.cineast)
        val exhibitHandler = ExhibitHandler(reader, writer, docRoot)

        // API endpoint.
        val endpoint = Javalin.create { conf ->
            conf.defaultContentType = "application/json"
            conf.enableCorsForAllOrigins()
            conf.server { setupHttpServer(config) }
            conf.enforceSsl = config.server.enableSsl

            conf.addStaticFiles { staticFiles ->
                staticFiles.location = Location.EXTERNAL
                staticFiles.directory = "./data"
            }

            conf.jsonMapper(KotlinXJsonMapper)

            // Logger.
            /*conf.requestLogger { ctx, ms ->
                logger.info { "Request received: ${ctx.req.requestURI}" }
            }*/
        }.routes {
            path("/exhibitions") {
                path("list") {
                    get(exhibitionHandler::listExhibitions)
                }
                path("load/{id}") {
                    get(exhibitionHandler::loadExhibitionById)
                }
                path("loadbyname/{:name}") {
                    get(exhibitionHandler::loadExhibitionByName)
                }
                path("save") {
                    post(exhibitionHandler::saveExhibition)
                }
            }
            path("/content/") {
                path("get/{path}") {
                    get(contentHandler::serveContent)
                }
                path("get/") {
                    get(contentHandler::serveContentBody)
                }
            }
            path("/exhibits") {
                path("list") {
                    get(exhibitHandler::listExhibits)
                }
                path("upload") {
                    post(exhibitHandler::saveExhibit)
                }
            }
        }

        endpoint.exception(Exception::class.java) { e, ctx ->
            logger.error(e) { "Exception occurred, sending 500 and exception name." }
            ctx.status(500)
                    .json(ErrorResponse("Error of type ${e.javaClass.simpleName} occurred. Check server log for additional information."))
        }
        endpoint.after { ctx ->
            ctx.header("Access-Control-Allow-Origin", "*")
            ctx.header("Access-Control-Allow-Headers", "*")
        }
        endpoint.start(config.server.httpPort)

        println("Started the server.")
        println("Ctrl+C to stop the server.")

        // TODO CLI to process commands (/quit and the like).
    }

    private fun setupHttpServer(config: Config): Server {

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

        /*
         * Straight from https://www.eclipse.org/jetty/documentation/jetty-11/programming_guide.php encrypted http/2
         */
        if (config.server.enableSsl) {
            val httpsConfig = HttpConfiguration(httpConfig).apply {
                addCustomizer(SecureRequestCustomizer())
            }

            val fallback = HttpConnectionFactory(httpsConfig)

            val alpn = ALPNServerConnectionFactory().apply {
                defaultProtocol = fallback.protocol
            }

            val sslContextFactory = SslContextFactory.Server().apply {
                keyStorePath = config.server.keystorePath
                setKeyStorePassword(config.server.keystorePassword)
                //cipherComparator = HTTP2Cipher.COMPARATOR
                provider = "Conscrypt"
            }

            val ssl = SslConnectionFactory(sslContextFactory, alpn.protocol)

            val http2 = HTTP2ServerConnectionFactory(httpsConfig)

            return Server(threadPool).apply {
                //HTTP Connector
                addConnector(ServerConnector(server, HttpConnectionFactory(httpConfig), HTTP2ServerConnectionFactory(httpConfig)).apply {
                    port = config.server.httpPort
                })
                // HTTPS Connector
                addConnector(ServerConnector(server, ssl, alpn, http2, fallback).apply {
                    port = config.server.httpsPort
                })
            }
        } else {
            return Server(threadPool).apply {
                //HTTP Connector
                addConnector(ServerConnector(server, HttpConnectionFactory(httpConfig), HTTP2ServerConnectionFactory(httpConfig)).apply {
                    port = config.server.httpPort
                })

            }
        }
    }

}
