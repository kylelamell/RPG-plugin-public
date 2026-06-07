package com.rpgle.plugin

import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.psi.RpgTokenTypes

/**
 * Regression for the "typed EXEC SQL block isn't highlighted until the file is
 * closed and reopened" bug. The editor's lexer-driven highlighter re-lexes
 * incrementally, handing the lexer a bounded read window. The EXEC SQL introducer
 * must therefore be recognized with no lookahead (EXEC alone enters a bridge
 * state, SQL then flips into IN_SQL). Before the fix the incremental re-lexer read
 * a freshly typed EXEC as a plain identifier, never entered the SQL state, and the
 * SQL stayed uncolored until a full reparse on reopen.
 *
 * This drives the real editor highlighter (not the PSI/annotator path, which
 * always re-lexes the whole file and so never reproduced the bug).
 */
class RpgIncrementalSqlHighlightTest : BasePlatformTestCase() {

    private fun tokenTypeAt(offset: Int) =
        (myFixture.editor as EditorEx).highlighter.createIterator(offset).tokenType

    fun testTypedExecSqlBlockIsHighlightedWithoutReopen() {
        myFixture.configureByText(
            "a.sqlrpgle",
            """
            **free
            dcl-proc run;
              <caret>
            end-proc;
            """.trimIndent()
        )

        // Type a brand-new SQL statement into the already-open file.
        myFixture.type("exec sql select name from t;")

        val text = myFixture.editor.document.charsSequence.toString()
        for (word in listOf("exec", "sql", "select", "from")) {
            val offset = text.indexOf(word)
            assertTrue("'$word' should be present in the document", offset >= 0)
            assertEquals(
                "freshly typed '$word' must lex as SQL_KEYWORD live, without reopening the file",
                RpgTokenTypes.SQL_KEYWORD,
                tokenTypeAt(offset),
            )
        }
    }
}
