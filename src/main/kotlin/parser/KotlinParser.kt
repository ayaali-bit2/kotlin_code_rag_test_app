package parser

import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import java.io.File

enum class DeclarationType {
    CLASS,
    FUNCTION
}

data class CodeChunk(
    val type: DeclarationType,
    val name: String,
    val content: String
)

object KotlinParser {

    fun parseFile(file: File): List<CodeChunk> {
        val disposable = Disposer.newDisposable()
        val environment = createEnvironment(disposable)

        return try {
            val psiFile = createPsiFile(environment, file)
            extractChunks(psiFile)
        } finally {
            Disposer.dispose(disposable)
        }
    }

    private fun createEnvironment(disposable: Disposable): KotlinCoreEnvironment {
        val configuration = CompilerConfiguration().apply {
            put(
                CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY,
                org.jetbrains.kotlin.cli.common.messages.MessageCollector.NONE
            )
        }

        return KotlinCoreEnvironment.createForProduction(
            disposable,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
    }

    private fun createPsiFile(environment: KotlinCoreEnvironment, file: File): KtFile =
        PsiFileFactory.getInstance(environment.project)
            .createFileFromText(
                file.name,
                KotlinLanguage.INSTANCE,
                file.readText()
            ) as KtFile

    private fun extractChunks(psiFile: KtFile): List<CodeChunk> =
        psiFile.declarations.mapNotNull { mapDeclaration(it) }

    private fun mapDeclaration(declaration: KtDeclaration): CodeChunk? = when (declaration) {
        is KtClass -> CodeChunk(
            type = DeclarationType.CLASS,
            name = declaration.name ?: "UnnamedClass",
            content = declaration.text
        )

        is KtNamedFunction -> CodeChunk(
            type = DeclarationType.FUNCTION,
            name = declaration.name ?: "UnnamedFunction",
            content = declaration.text
        )

        else -> null
    }
}
