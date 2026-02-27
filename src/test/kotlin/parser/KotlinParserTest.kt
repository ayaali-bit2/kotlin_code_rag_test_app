package parser

import java.io.File
import kotlin.io.path.createTempFile
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KotlinParserTest {

    @Test
    fun `parseFile loads declarations from multiple files`() {
        val firstFile = createTempKotlinFile(
            """
            class FirstSample

            fun firstSampleFunction() {}
            """.trimIndent()
        )
        val secondFile = createTempKotlinFile(
            """
            class SecondSample

            fun secondSampleFunction() {}
            """.trimIndent()
        )

        val firstChunks = KotlinParser.parseFile(firstFile)
        val secondChunks = KotlinParser.parseFile(secondFile)

        assertEquals(2, firstChunks.size)
        assertEquals(2, secondChunks.size)
        assertTrue(firstChunks.any { it.name == "FirstSample" })
        assertTrue(firstChunks.any { it.name == "firstSampleFunction" })
        assertTrue(secondChunks.any { it.name == "SecondSample" })
        assertTrue(secondChunks.any { it.name == "secondSampleFunction" })
    }

    @Test
    fun `parseFile recognizes interfaces enums and data classes`() {
        val file = createTempKotlinFile(
            """
            interface SampleInterface {
                fun execute(): String
            }

            enum class SampleStatus {
                PENDING,
                COMPLETED
            }

            data class SampleData(
                val id: Int,
                val label: String
            )
            """.trimIndent()
        )

        val chunks = KotlinParser.parseFile(file)

        assertTrue(chunks.any { it.type == DeclarationType.INTERFACE && it.name == "SampleInterface" })
        assertTrue(chunks.any { it.type == DeclarationType.ENUM && it.name == "SampleStatus" })
        assertTrue(chunks.any { it.type == DeclarationType.DATA_CLASS && it.name == "SampleData" })
    }

    @Test
    fun `parseFile returns empty list for empty file`() {
        val emptyFile = createTempKotlinFile("")

        assertTrue(KotlinParser.parseFile(emptyFile).isEmpty())
    }

    private fun createTempKotlinFile(content: String): File =
        createTempFile(suffix = ".kt").toFile().apply {
            writeText(content)
            deleteOnExit()
        }
}
