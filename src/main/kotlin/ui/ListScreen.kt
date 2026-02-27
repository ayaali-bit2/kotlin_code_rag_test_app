package ui

import parser.CodeChunk

object ListScreen {
    fun show(chunks: List<CodeChunk>) {
        println("\nParsed Kotlin declarations:")
        chunks.forEachIndexed { index, chunk ->
            val typeLabel = chunk.type.name.lowercase().replaceFirstChar { it.uppercaseChar() }
            println("${index + 1}. $typeLabel — ${chunk.name}")
            val previewLines = chunk.content
                .lineSequence()
                .map(String::trim)
                .filter { it.isNotBlank() }
                .take(3)
                .map { "    $it" }
                .joinToString("\n")
            if (previewLines.isNotEmpty()) {
                println(previewLines)
            }
            println()
        }
    }
}
