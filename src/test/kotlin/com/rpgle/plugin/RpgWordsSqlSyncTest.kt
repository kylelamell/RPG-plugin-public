package com.rpgle.plugin

import com.rpgle.plugin.data.RpgWords
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Guards the deliberate duplication between the lexer and the Kotlin vocabulary:
 * [RpgWords.SQL_KEYWORDS] / [RpgWords.SQL_CURSOR_KEYWORDS] are human-readable
 * mirrors of the `SQL_KEYWORD` / `SQL_CURSOR_KW` macros baked into
 * `RpgLexer.flex`. JFlex can't read Kotlin at lex time, so the two can drift —
 * this test fails the moment they do.
 */
class RpgWordsSqlSyncTest {

    @Test
    fun sqlKeywordsMatchTheLexerMacro() {
        assertEquals(
            "RpgWords.SQL_KEYWORDS must match the SQL_KEYWORD macro in RpgLexer.flex",
            macroLiterals("SQL_KEYWORD"),
            RpgWords.SQL_KEYWORDS,
        )
    }

    @Test
    fun sqlCursorKeywordsMatchTheLexerMacro() {
        assertEquals(
            "RpgWords.SQL_CURSOR_KEYWORDS must match the SQL_CURSOR_KW macro in RpgLexer.flex",
            macroLiterals("SQL_CURSOR_KW"),
            RpgWords.SQL_CURSOR_KEYWORDS,
        )
    }

    /**
     * Extracts the quoted literals of a JFlex macro definition that looks like
     * `NAME = "A" | "B"` possibly continued over several `| "C"` lines.
     */
    private fun macroLiterals(macroName: String): Set<String> {
        val lines = flexSource().lines()
        val defLine = Regex("""^\s*${Regex.escape(macroName)}\s*=""")
        val start = lines.indexOfFirst { defLine.containsMatchIn(it) }
        require(start >= 0) { "macro $macroName not found in RpgLexer.flex" }

        val block = StringBuilder(lines[start].substringAfter('='))
        var i = start + 1
        while (i < lines.size && lines[i].trim().startsWith("|")) {
            block.append(' ').append(lines[i].trim())
            i++
        }
        return Regex("\"([^\"]+)\"").findAll(block)
            .map { it.groupValues[1].uppercase() }
            .toSet()
    }

    private fun flexSource(): String {
        val rel = "src/main/kotlin/com/rpgle/plugin/lexer/RpgLexer.flex"
        val base = File(System.getProperty("user.dir"))
        val file = generateSequence(base) { it.parentFile }
            .map { it.resolve(rel) }
            .firstOrNull { it.isFile }
            ?: error("RpgLexer.flex not found relative to ${base.absolutePath}")
        return file.readText()
    }
}
