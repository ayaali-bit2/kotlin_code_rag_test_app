package rag

import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.embedding.*
import com.aallam.openai.api.model.ModelId

import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt

data class EmbeddedChunk(
    val chunkText: String,
    val embedding: List<Double>
)

class RagEngine(private val apiKey: String) {

    private val openAI = OpenAI(apiKey)
    private val store = mutableListOf<EmbeddedChunk>()

    private fun cosine(a: List<Double>, b: List<Double>): Double {
        val dot = a.zip(b).sumOf { it.first * it.second }
        val normA = sqrt(a.sumOf { it * it })
        val normB = sqrt(b.sumOf { it * it })
        return dot / (normA * normB)
    }

    suspend fun indexChunks(chunks: List<String>) {
        for (chunk in chunks) {
            val embedding = openAI.embeddings(
                EmbeddingRequest(ModelId("text-embedding-3-small"), listOf(chunk))
            ).embeddings.first().embedding  // use .embeddings instead of .data
            store.add(EmbeddedChunk(chunk, embedding))
        }
    }



    suspend fun retrieve(query: String): String? {
        val queryEmbedding = openAI.embeddings(
            EmbeddingRequest(ModelId("text-embedding-3-small"), listOf(query))
        ).embeddings.first().embedding

        return store.maxByOrNull { cosine(it.embedding, queryEmbedding) }?.chunkText
    }

}
