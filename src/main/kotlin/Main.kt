import kotlinx.coroutines.runBlocking
import parser.CodeChunk
import parser.KotlinParser
import rag.RagEngine
import ui.ListScreen
import java.io.File

private const val CODE_SAMPLE_PATH = "src/main/kotlin/sample/TestFile.kt"
private const val API_KEY_ENV = "OPENAI_API_KEY"

fun main() = runBlocking {
    Application.run()
}

object Application {
    suspend fun run() {
        val kotlinFile = locateSampleFile()
        val codeChunks = KotlinParser.parseFile(kotlinFile)

        if (codeChunks.isEmpty()) {
            println("No Kotlin declarations were parsed from ${kotlinFile.name}.")
            return
        }

        ListScreen.show(codeChunks)

        val ragEngine = RagEngine(fetchApiKey())
        ragEngine.indexChunks(codeChunks)

        val question = promptForQuestion() ?: return
        val retrievedChunk = ragEngine.retrieve(question)

        displayResult(retrievedChunk, question)
    }

    private fun locateSampleFile(): File {
        val file = File(CODE_SAMPLE_PATH)
        require(file.exists()) {
            "Sample Kotlin file not found at ${file.absolutePath}. Please ensure the repository is cloned."
        }
        return file
    }

    private fun fetchApiKey(): String =
        System.getenv(API_KEY_ENV).takeUnless(String::isNullOrBlank) ?: "YOUR_API_KEY"

    private fun promptForQuestion(): String? {
        println("Ask a question about the Kotlin file:")
        val input = readLine()?.trim()
        if (input.isNullOrBlank()) {
            println("No question provided. Exiting.")
            return null
        }
        return input
    }

    private fun displayResult(chunk: CodeChunk?, question: String) {
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
