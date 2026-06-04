package com.rpgle.plugin

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.dds.DdsLexerAdapter
import com.rpgle.plugin.dds.DdsTokenTypes

class DdsLexerTest : BasePlatformTestCase() {

    private fun lex(text: String): List<Pair<IElementType, String>> {
        val lexer = DdsLexerAdapter()
        lexer.start(text)
        val result = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return result
    }

    fun testCommentLine() {
        val tokens = lex("     A* this is a comment")
        assertEquals(1, tokens.size)
        assertEquals(DdsTokenTypes.COMMENT, tokens[0].first)
    }

    fun testRecordLineIsNotAComment() {
        val tokens = lex("     A          R CUSTREC")
        assertFalse("a record line must not be treated as a comment",
            tokens.any { it.first == DdsTokenTypes.COMMENT })
    }

    fun testKeywordAndStringHighlighted() {
        val types = lex("     A          R CUSTREC                 TEXT('Customer')").map { it.first }
        assertTrue("expected TEXT as a DDS keyword", types.contains(DdsTokenTypes.KEYWORD))
        assertTrue("expected the quoted text as a string", types.contains(DdsTokenTypes.STRING))
    }

    fun testKeywordAndSpecialValue() {
        val types = lex("     A            ORDDATE        L         DATFMT(*ISO)").map { it.first }
        assertTrue("expected DATFMT as a DDS keyword", types.contains(DdsTokenTypes.KEYWORD))
        assertTrue("expected *ISO as a special value", types.contains(DdsTokenTypes.SPECIAL))
    }

    fun testLogicalFileKeyword() {
        val types = lex("     A          K CUSTNO\n     A            PFILE(CUSTMAST)").map { it.first }
        assertTrue("expected PFILE as a DDS keyword", types.contains(DdsTokenTypes.KEYWORD))
    }
}
