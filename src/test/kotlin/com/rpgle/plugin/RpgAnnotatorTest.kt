package com.rpgle.plugin

import com.intellij.codeInsight.daemon.impl.HighlightInfo
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.highlight.RpgSyntaxHighlighter

/**
 * Covers [com.rpgle.plugin.highlight.RpgAnnotator]'s semantic coloring, focused on
 * the embedded-SQL guard: an identifier inside an `EXEC SQL` block that collides
 * with an RPG word (a column named DATE / TIME / READ / …) must not be colored as
 * an RPG keyword, while the same word outside SQL still is.
 */
class RpgAnnotatorTest : BasePlatformTestCase() {

    private fun infos(): List<HighlightInfo> =
        myFixture.doHighlighting(HighlightSeverity.INFORMATION)

    /** Annotator infos that color [word] (case-insensitive) with [key]. */
    private fun colored(word: String, key: TextAttributesKey): List<HighlightInfo> {
        val text = myFixture.file.text
        return infos().filter { info ->
            info.forcedTextAttributesKey == key &&
                text.substring(info.startOffset, info.endOffset).equals(word, ignoreCase = true)
        }
    }

    fun testSqlColumnMatchingRpgDataTypeIsNotColoredAsRpg() {
        myFixture.configureByText(
            "a.sqlrpgle",
            """
            **free
            dcl-proc run;
              exec sql select date, time into :d, :t from mytable;
            end-proc;
            """.trimIndent()
        )
        assertEmpty(
            "a column named DATE inside EXEC SQL must not be colored as an RPG data type",
            colored("date", RpgSyntaxHighlighter.DATA_TYPE),
        )
        assertEmpty(
            "a column named TIME inside EXEC SQL must not be colored as an RPG data type",
            colored("time", RpgSyntaxHighlighter.DATA_TYPE),
        )
    }

    fun testDataTypeOutsideSqlIsStillColored() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-s stamp timestamp;
            """.trimIndent()
        )
        assertNotEmpty(colored("timestamp", RpgSyntaxHighlighter.DATA_TYPE))
    }

    fun testOpcodeOutsideSqlIsStillColored() {
        // Sanity check that the hoisted SQL guard didn't suppress ordinary RPG
        // opcode coloring in a file that also contains no SQL.
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-proc run;
              if x = 1;
              endif;
            end-proc;
            """.trimIndent()
        )
        assertNotEmpty(colored("if", RpgSyntaxHighlighter.OPCODE))
    }
}
