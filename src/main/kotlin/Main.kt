import kotlinx.coroutines.runBlocking
import parser.KotlinParser
import rag.RagEngine
import presentation.DeclarationPresenter
import ui.ConsoleView
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
            ConsoleView.showNoDeclarationsFound(kotlinFile.name)
            return
        }

        val presenter = DeclarationPresenter(RagEngine(fetchApiKey()))
        presenter.indexChunks(codeChunks)

        ConsoleView.showDeclarationGroups(presenter.groupDeclarationsByType(codeChunks))
        val question = ConsoleView.promptForQuestion() ?: return
        val retrievedChunk = presenter.retrieveRelevantChunk(question)

        ConsoleView.showRetrievalResult(retrievedChunk, question)
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
}
