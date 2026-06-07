package com.rpgle.plugin

import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.binder.BinderLexerAdapter
import com.rpgle.plugin.binder.BinderTokenTypes

class BinderLexerTest : BasePlatformTestCase() {

    private fun lex(text: String): List<Pair<IElementType, String>> {
        val lexer = BinderLexerAdapter()
        lexer.start(text)
        val result = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return result
    }

    fun testKeywordSpecialStringAndComment() {
        val types = lex("STRPGMEXP PGMLVL(*CURRENT) SIGNATURE('ABC123') // export list")
            .map { it.first }
        assertTrue("expected a binder KEYWORD", types.contains(BinderTokenTypes.KEYWORD))
        assertTrue("expected a *SPECIAL value", types.contains(BinderTokenTypes.SPECIAL))
        assertTrue("expected a STRING", types.contains(BinderTokenTypes.STRING))
        assertTrue("expected a COMMENT", types.contains(BinderTokenTypes.COMMENT))
    }

    fun testExportSymbolIdentifier() {
        val tokens = lex("EXPORT SYMBOL(myProc)")
        assertEquals(BinderTokenTypes.KEYWORD, tokens.first().first)
        assertTrue(
            "expected myProc as an identifier, got $tokens",
            tokens.any { it.first == BinderTokenTypes.IDENTIFIER && it.second == "myProc" },
        )
    }
}
