package ui

import parser.CodeChunk
import presentation.DeclarationGroup

object ConsoleView {

    fun showNoDeclarationsFound(filename: String) {
        println("No Kotlin declarations were parsed from $filename.")
    }

    fun showDeclarationGroups(groups: List<DeclarationGroup>) {
        if (groups.isEmpty()) return

        println("\nParsed Kotlin declarations grouped by type:")
        groups.forEach { group ->
            val groupLabel = group.type.name.lowercase().replaceFirstChar { it.uppercaseChar() }
            println("\n$groupLabel declarations (${group.declarations.size}):")
            group.declarations.forEach { declaration ->
                println("  - ${declaration.name}")
            }
        }
    }

    fun promptForQuestion(): String? {
        println("\nAsk a question about the Kotlin file:")
        val input = readLine()?.trim()
        if (input.isNullOrBlank()) {
            println("No question provided. Exiting.")
            return null
        }
        return input
    }

    fun showRetrievalResult(chunk: CodeChunk?, question: String) {
        if (chunk == null) {
            println("Unable to find a relevant declaration for \"$question\".")
            return
        }

        println("\nRetrieved declaration:")
        println("Type: ${chunk.type.name.lowercase()}")
        println("Name: ${chunk.name}")
        println("Content:\n${chunk.content.trim()}")
    }
}
