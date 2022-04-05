package ch.unibas.dmi.dbis.vrem.import

import ch.unibas.dmi.dbis.vrem.config.Config
import ch.unibas.dmi.dbis.vrem.database.VREMDao
import ch.unibas.dmi.dbis.vrem.import.ImportUtils.calculateExhibitSize
import ch.unibas.dmi.dbis.vrem.import.ImportUtils.calculateWallExhibitPosition
import ch.unibas.dmi.dbis.vrem.model.exhibition.*
import ch.unibas.dmi.dbis.vrem.model.math.Vector3f
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.float
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.util.stream.Stream
import kotlin.io.path.extension
import kotlin.io.path.isDirectory
import kotlin.io.path.relativeTo
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * Importer for exhibition folders.
 * To be used directly as CLI command; the exhibition should include everything as in the
 * [sample exhibition](git@github.com:VIRTUE-DBIS/vre-mixnhack19.git).
 */
class ExhibitionFolderImporter : CliktCommand(name = "import-folder", help = "Imports a folder-based exhibition.") {

    val exhibitionPath by option(
        "-p",
        "--path",
        help = "Path to the exhibition root folder (where the room folders reside)."
    ).required()

    val config by option(
        "-c",
        "--config",
        help = "Path to the configuration file to use (defaults to config.json)."
    ).default("config.json")

    val clean by option(
        "--clean", help = "Overrides old exhibitions with the same name from the database."
    ).flag("--keep", default = false)

    val exhibitionDescription by option(
        "-d",
        "--description",
        help = "Description of the exhibition (defaults to empty)."
    ).default("")

    val name by option(
        "-n",
        "--name",
        help = "The name of the exhibition (must be unique)."
    ).default("")

    val ignore by option(
        "--ignore",
        help = "A prefix for folders to ignore instead of treating them as a room folders (defaults to __)."
    ).default("__")

    val defaultLongSide: Float by option(
        "--default-long-side",
        help = "The length of the longer side of an image in meters (defaults to 2.0)."
    ).float().default(2f)

    private lateinit var exhibition: Exhibition
    private lateinit var importRoot: File
    private lateinit var storageRoot: File

    companion object {
        private val json = Json {
//            serializersModule = IdKotlinXSerializationModule
            encodeDefaults = true
        }
    }

    override fun run() {
        // Get config and a reader/writer for the database.
        val config = Config.readConfig(this.config)
        val (reader, writer) = VREMDao.getDAOs(config.database)
        this.importRoot = File(exhibitionPath)

        // Checks: Require exhibition folder to exist and no other exhibition with the same name to exist.
        if (!importRoot.exists() and !importRoot.isDirectory) {
            logger.error { "--path argument has to point to an existing directory!" }
            exitProcess(-1)
        }

        if (reader.existsExhibition(name)) {
            if (!clean) {
                logger.error { "An exhibition with name $name already exists!" }
                exitProcess(-2)
            }

            // If we made it here we can delete the exhibition from the collection.
            writer.deleteExhibition(name)
            logger.info { "Successfully removed previously existing exhibition '$name'." }
        }

        // Create new exhibition.
        this.exhibition = Exhibition(name = name, description = exhibitionDescription)

        // Roots for import and storage.
        this.storageRoot = File(config.server.documentRoot).resolve(this.exhibition.id)

        val validFolders = this.importRoot.listFiles()!!.filter { !it.nameWithoutExtension.startsWith(ignore) }

        // Determine path to copy media files to after importing.

        logger.info { "Starting import exhibition at ${this.importRoot}." }

        // Try to add every folder as a new room to the previously created exhibition object.
        validFolders.forEach { exhibition.addRoom(importRoom(it, exhibition.rooms)) }

        logger.info { "Writing exhibition entry to MongoDB..." }

        // Create MongoDB entry.
        writer.saveExhibition(exhibition)

        logger.info { "Copying files..." }

        validFolders.forEach { copyRoomFolder(it.toPath()) }

        logger.info { "Finished import." }
    }

    private fun copyRoomFolder(folder: Path) {
        // Get everything that's not JSON.
        val files: Stream<Path> =
            Files.walk(folder).filter { !it.isDirectory() && it.extension != ImportUtils.JSON_EXTENSION }

        // Copy everything.
        for (f in files) {
            val targetPath = storageRoot.toPath().resolve(f.relativeTo(importRoot.toPath()))

            // Create directories if they don't exist.
            Files.createDirectories(targetPath.parent)

            // Copy the files.
            Files.copy(f, targetPath)
        }
    }

    /**
     * Tries to import a single exhibit.
     *
     * @param exhibitFile The exhibit file (e.g., an image).
     * @param siblings Sibling exhibits (in the same room).
     * @return The imported exhibit.
     */
    private fun importExhibit(exhibitFile: File, siblings: List<Exhibit>): Exhibit {
        logger.debug { "Importing exhibit $exhibitFile." }

        // Try to import exhibit by reading image and, if available, its config.
        val exhibit = readExhibitConfigOrCreateNew(exhibitFile)

        // No valid size given: Calculate exhibit size.
        if (exhibit.size.isNaN() or (exhibit.size == Vector3f.ORIGIN)) {
            calculateExhibitSize(exhibitFile.readBytes(), exhibit, defaultLongSide)
        }

        // Calculate exhibit position if not given.
        if (exhibit.position.isNaN() or (exhibit.position == Vector3f.ORIGIN)) {
            exhibit.position = calculateWallExhibitPosition(exhibit, siblings)
        }

        return exhibit
    }

    /**
     * Imports a room.
     *
     * @param roomFile The path of the room.
     * @param siblings Sibling rooms, used to calculate a room's position.
     * @return The created room.
     */
    private fun importRoom(roomFile: File, siblings: List<Room>): Room {
        logger.debug { "Importing room $roomFile." }

        val room = readRoomConfigOrCreateNew(roomFile)

        for (dir in Direction.values()) {
            room.setWall(dir, importWall(dir, roomFile.resolve(dir.toString().lowercase())))
        }

        room.position = ImportUtils.calculateRoomPosition(room, siblings)

        return room
    }

    /**
     * Imports a wall and its exhibits.
     *
     * @param dir The direction the wall is facing when looking from the origin.
     * @param wallFolder The path of the wall to read the config for.
     * @return The wall (with the imported exhibits).
     */
    private fun importWall(dir: Direction, wallFolder: File): Wall {
        logger.debug { "Importing wall $wallFolder." }

        val wall = readWallConfigOrCreateNew(dir, wallFolder)

        wallFolder.listFiles()
            ?.filter { ImportUtils.IMAGE_FILE_EXTENSIONS.contains(it.extension.lowercase()) }
            ?.forEach { wall.placeExhibit(importExhibit(it, wall.exhibits)) }

        return wall
    }

    /**
     * Reads an exhibit configuration, creating a new one with default settings if the file was not found.
     *
     * @param exhibitFile The path of the exhibit to read the config for.
     * @return The exhibit as defined in the (potentially defaulted) configuration.
     */
    private fun readExhibitConfigOrCreateNew(exhibitFile: File): Exhibit {
        val configFile =
            exhibitFile.parentFile.resolve("${exhibitFile.nameWithoutExtension}.${ImportUtils.JSON_EXTENSION}")

        logger.debug { "Looking for exhibit configuration at $configFile." }

        val exhibitPath =
            this.exhibition.id + "/" + exhibitFile.relativeTo(importRoot).toString().replace('\\', '/')

        return if (configFile.exists()) {
            val exhibit = json.decodeFromString(Exhibit.serializer(), configFile.readText())

            exhibit.path = exhibitPath

            if (exhibit.audio != null) {
                exhibit.audio = exhibitPath.substringBeforeLast("/") + "/" + exhibit.audio
            }

            exhibit
        } else {
            Exhibit(exhibitFile.nameWithoutExtension, exhibitPath, CulturalHeritageObject.Companion.CHOType.IMAGE)
        }
    }

    /**
     * Reads a wall configuration, creating a new one with default settings if the file was not found.
     *
     * @param dir The direction the wall is facing when looking from the origin.
     * @param wallFolder The path of the wall to read the config for.
     * @return The wall as defined in the (potentially defaulted) configuration.
     */
    private fun readWallConfigOrCreateNew(dir: Direction, wallFolder: File): Wall {
        val wallConfigFile = wallFolder.resolve(ImportUtils.WALL_CONFIG_FILE)

        logger.debug { "Looking for wall configuration at $wallConfigFile." }

        return if (wallConfigFile.exists()) {
            val wall = json.decodeFromString(Wall.serializer(), wallConfigFile.readText())

            wall.direction = dir
            wall
        } else {
            Wall(dir)
        }
    }

    /**
     * Reads a room configuration, creating a new one with default settings if the file was not found.
     *
     * @param room The path of the room to read the config for.
     * @return The room as defined in the (potentially defaulted) configuration.
     */
    private fun readRoomConfigOrCreateNew(room: File): Room {
        logger.debug { "Looking for room configuration at $room." }

        val roomConfigFile = room.resolve(ImportUtils.ROOM_CONFIG_FILE)

        return if (roomConfigFile.exists()) {
            json.decodeFromString(Room.serializer(), roomConfigFile.readText())
        } else {
            Room(text = room.name)
        }
    }

}
