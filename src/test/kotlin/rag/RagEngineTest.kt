package rag

import kotlinx.coroutines.runBlocking
import parser.CodeChunk
import parser.DeclarationType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

private class TestEmbeddingProvider(
    private val forcedEmbeddings: Map<String, List<Double>?> = emptyMap()
) : EmbeddingProvider {

    override suspend fun embed(text: String): List<Double>? {
        if (forcedEmbeddings.containsKey(text)) {
            return forcedEmbeddings[text]
        }

        if (text.isBlank()) return null

        val normalized = text.lowercase()
        val alphaScore = if (containsKeyword(normalized, "alpha")) 1.0 else 0.0
        val betaScore = if (containsKeyword(normalized, "beta")) 1.0 else 0.0

        return listOf(alphaScore, betaScore)
    }

    private fun containsKeyword(source: String, keyword: String): Boolean =
        source.contains(keyword) || keyword.contains(source)
}

class RagEngineTest {

    @Test
    fun `retrieve returns null when query embedding cannot be generated`() = runBlocking {
        val provider = TestEmbeddingProvider(mapOf("no-match-query" to null))
        val ragEngine = RagEngine(provider)
        val alphaChunk = CodeChunk(DeclarationType.CLASS, "AlphaClass", "class AlphaClass {}")

        ragEngine.indexChunks(listOf(alphaChunk))

        assertNull(ragEngine.retrieve("no-match-query"))
    }

    @Test
    fun `retrieve honors partial query matches`() = runBlocking {
        val provider = TestEmbeddingProvider()
        val ragEngine = RagEngine(provider)

        val alphaChunk = CodeChunk(DeclarationType.CLASS, "AlphaClass", "class AlphaClass {}")
        val betaChunk = CodeChunk(DeclarationType.CLASS, "BetaClass", "class BetaClass {}")

        ragEngine.indexChunks(listOf(alphaChunk, betaChunk))

        val retrieved = ragEngine.retrieve("alpha")

        assertNotNull(retrieved)
        assertEquals("AlphaClass", retrieved.name)
    }
}
