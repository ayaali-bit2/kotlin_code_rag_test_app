package parser

import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.com.intellij.openapi.Disposable
import org.jetbrains.kotlin.com.intellij.openapi.util.Disposer
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtClass
import java.io.File
import org.jetbrains.kotlin.com.intellij.psi.PsiFileFactory
import org.jetbrains.kotlin.idea.KotlinLanguage


data class CodeChunk(
    val type: String,
    val name: String,
    val content: String
)

object KotlinParser {

    fun parseFile(file: File): List<CodeChunk> {

        val disposable: Disposable = Disposer.newDisposable()
        val configuration = CompilerConfiguration()
        configuration.put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, org.jetbrains.kotlin.cli.common.messages.MessageCollector.NONE)

        val environment = KotlinCoreEnvironment.createForProduction(
            disposable,
            configuration,
            EnvironmentConfigFiles.JVM_CONFIG_FILES
        )

        val psiFile = PsiFileFactory.getInstance(environment.project)
            .createFileFromText(
                file.name,
                KotlinLanguage.INSTANCE,
                file.readText()
            ) as KtFile


        val chunks = mutableListOf<CodeChunk>()

        psiFile.declarations.forEach { declaration ->

            when (declaration) {

                is KtClass -> {
                    chunks.add(
                        CodeChunk(
                            type = "class",
                            name = declaration.name ?: "UnnamedClass",
                            content = declaration.text
                        )
                    )
                }

                is KtNamedFunction -> {
                    chunks.add(
                        CodeChunk(
                            type = "function",
                            name = declaration.name ?: "UnnamedFunction",
                            content = declaration.text
                        )
                    )
                }
            }
        }

        Disposer.dispose(disposable)
        return chunks
    }
}
