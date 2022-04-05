package ch.unibas.dmi.dbis.vrem

import ch.unibas.dmi.dbis.vrem.import.ExhibitionFolderImporter
import ch.unibas.dmi.dbis.vrem.rest.API
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.ExperimentalSerializationApi

/**
 * VREM Top-level CLI call.
 */
class VREM : CliktCommand(name = "vrem", help = "This is the virtual-exhibition-manager (VREM)") {

    override fun run() = Unit // Top-level command (without args), not executable on its own.

}

/**
 * Main entry point for VREM.
 *
 * @param args The (command line) arguments for the CLI parser.
 */
@ExperimentalSerializationApi
fun main(args: Array<String>) = VREM().subcommands(API(), ExhibitionFolderImporter()).main(args)
