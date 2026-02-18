package rag

import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.model.ModelId
import parser.CodeChunk
import kotlin.math.sqrt

private val DEFAULT_MODEL = ModelId("text-embedding-3-small")

private data class EmbeddedChunk(
    val sourceChunk: CodeChunk,
    val embedding: List<Double>
)

class RagEngine(private val apiKey: String) {

    private val openAI = OpenAI(apiKey)
    private val store = mutableListOf<EmbeddedChunk>()

    private fun cosineSimilarity(a: List<Double>, b: List<Double>): Double {
        val dotProduct = a.zip(b).sumOf { (first, second) -> first * second }
        val normA = sqrt(a.sumOf { it * it })
        val normB = sqrt(b.sumOf { it * it })
        return if (normA == 0.0 || normB == 0.0) 0.0 else dotProduct / (normA * normB)
    }

    suspend fun indexChunks(chunks: List<CodeChunk>) {
        chunks.forEach { chunk ->
            val embedding = openAI.embeddings(
                EmbeddingRequest(DEFAULT_MODEL, listOf(chunk.content))
            ).embeddings.firstOrNull()?.embedding ?: return@forEach
            store.add(EmbeddedChunk(chunk, embedding))
        }
    }

    suspend fun retrieve(query: String): CodeChunk? {
        if (store.isEmpty()) return null
        val queryEmbedding = openAI.embeddings(
            EmbeddingRequest(DEFAULT_MODEL, listOf(query))
        ).embeddings.firstOrNull()?.embedding ?: return null
        return store.maxByOrNull { cosineSimilarity(it.embedding, queryEmbedding) }?.sourceChunk
    }

    fun clearStore() {
        store.clear()
    }
}
