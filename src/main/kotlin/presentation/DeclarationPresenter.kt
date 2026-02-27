package presentation

import parser.CodeChunk
import parser.DeclarationType
import rag.RagEngine

/**
 * Groups and surfaces parsed Kotlin declarations for the UI layer.
 */
data class DeclarationGroup(
    val type: DeclarationType,
    val declarations: List<CodeChunk>
)

class DeclarationPresenter(private val ragEngine: RagEngine) {

    suspend fun indexChunks(chunks: List<CodeChunk>) {
        ragEngine.clearStore()
        ragEngine.indexChunks(chunks)
    }

    fun groupDeclarationsByType(chunks: List<CodeChunk>): List<DeclarationGroup> =
        DeclarationType.values()
            .map { type ->
                DeclarationGroup(
                    type = type,
                    declarations = chunks
                        .filter { it.type == type }
                        .sortedBy { it.name }
                )
            }
            .filter { it.declarations.isNotEmpty() }

    suspend fun retrieveRelevantChunk(query: String): CodeChunk? =
        ragEngine.retrieve(query)
}
