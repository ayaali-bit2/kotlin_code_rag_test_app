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
