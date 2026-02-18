import com.aallam.openai.client.OpenAI
import com.aallam.openai.api.embedding.EmbeddingRequest
import com.aallam.openai.api.model.ModelId
import kotlinx.coroutines.runBlocking
import kotlin.math.sqrt
import java.io.File

data class EmbeddedChunk(val text: String, val embedding: List<Double>)

class RagEngine(apiKey: String) {
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

        return store.maxByOrNull { cosine(it.embedding, queryEmbedding) }?.text
    }


}

fun main() = runBlocking {
    val apiKey = "YOUR_API_KEY"
    val file = File("C:/Users/Aya Ali/IdeaProjects/kotlin_code_rag_test_app/src/main/kotlin/sample/TestFile.kt")

    val chunks = file.readText()
        .split(Regex("(?=\\bfun\\b|\\bclass\\b)"))
        .filter { it.isNotBlank() }

    val rag = RagEngine(apiKey)
    rag.indexChunks(chunks)

    println("Ask a question about the Kotlin file:")
    val question = readLine()!!
    val result = rag.retrieve(question)
    println("\nRetrieved chunk:\n$result")

}
