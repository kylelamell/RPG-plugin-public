package com.rpgle.plugin

import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.lexer.RpgLexerAdapter
import com.rpgle.plugin.psi.RpgTokenTypes

class RpgLexerTest : BasePlatformTestCase() {

    private fun lex(text: String): List<Pair<IElementType, String>> {
        val lexer = RpgLexerAdapter()
        lexer.start(text)
        val result = ArrayList<Pair<IElementType, String>>()
        while (lexer.tokenType != null) {
            result.add(lexer.tokenType!! to text.substring(lexer.tokenStart, lexer.tokenEnd))
            lexer.advance()
        }
        return result
    }

    fun testHyphenatedKeywordIsSingleToken() {
        val tokens = lex("dcl-proc")
        assertEquals(1, tokens.size)
        assertEquals(RpgTokenTypes.IDENTIFIER, tokens[0].first)
        assertEquals("dcl-proc", tokens[0].second)
    }

    fun testHyphenatedOpcodeIsSingleToken() {
        val tokens = lex("z-add")
        assertEquals(1, tokens.size)
        assertEquals(RpgTokenTypes.IDENTIFIER, tokens[0].first)
        assertEquals("z-add", tokens[0].second)
    }

    fun testBifStringAndComment() {
        val types = lex("x = %trim('a''b'); // note").map { it.first }
        assertTrue("expected a BIF token", types.contains(RpgTokenTypes.BIF))
        assertTrue("expected a STRING token", types.contains(RpgTokenTypes.STRING))
        assertTrue("expected a COMMENT token", types.contains(RpgTokenTypes.COMMENT))
    }

    fun testCopyDirective() {
        val tokens = lex("/copy qrpgleref,custproc")
        assertEquals(RpgTokenTypes.DIRECTIVE, tokens.first().first)
        assertEquals("/copy", tokens.first().second)
    }

    fun testMinusIsOperatorNotPartOfName() {
        val types = lex("a-b").map { it.first }
        assertTrue(types.contains(RpgTokenTypes.OPERATOR))
    }

    fun testPeriodInQualifiedNameIsDotNotBadCharacter() {
        val tokens = lex("total = customer.balance;")
        assertTrue(
            "expected a DOT token for the period, got $tokens",
            tokens.any { it.first == RpgTokenTypes.DOT && it.second == "." },
        )
        assertFalse(
            "period must not be a bad character, got $tokens",
            tokens.any { it.first == TokenType.BAD_CHARACTER },
        )
    }

    fun testExecSqlKeywordsAreHighlighted() {
        val tokens = lex("exec sql select name from custmast where id = 5;")
        val sqlKeywords = tokens
            .filter { it.first == RpgTokenTypes.SQL_KEYWORD }
            .map { it.second.lowercase() }
        assertTrue("expected SELECT highlighted, got $sqlKeywords", sqlKeywords.contains("select"))
        assertTrue("expected FROM highlighted, got $sqlKeywords", sqlKeywords.contains("from"))
        assertTrue("expected WHERE highlighted, got $sqlKeywords", sqlKeywords.contains("where"))
        assertTrue(tokens.any { it.first == RpgTokenTypes.IDENTIFIER && it.second.equals("custmast", true) })
    }

    fun testSemicolonLeavesSqlState() {
        val tokens = lex("exec sql select * from t;\nselect = 1;")
        val selects = tokens.filter { it.second.equals("select", true) }
        assertEquals("expected two 'select' words", 2, selects.size)
        assertEquals(RpgTokenTypes.SQL_KEYWORD, selects[0].first)
        assertEquals(RpgTokenTypes.IDENTIFIER, selects[1].first)
    }

    fun testCursorKeywordsAreDistinctFromSqlKeywords() {
        val tokens = lex("exec sql declare c1 cursor for select name from t;")
        val cursorKw = tokens
            .filter { it.first == RpgTokenTypes.SQL_CURSOR_KEYWORD }
            .map { it.second.lowercase() }
        assertTrue("expected DECLARE as a cursor keyword, got $cursorKw", cursorKw.contains("declare"))
        assertTrue("expected CURSOR as a cursor keyword, got $cursorKw", cursorKw.contains("cursor"))

        val sqlKw = tokens
            .filter { it.first == RpgTokenTypes.SQL_KEYWORD }
            .map { it.second.lowercase() }
        assertTrue("expected FOR as a general SQL keyword, got $sqlKw", sqlKw.contains("for"))
        assertTrue("expected SELECT as a general SQL keyword, got $sqlKw", sqlKw.contains("select"))
        assertFalse("FOR must not be a cursor keyword, got $cursorKw", cursorKw.contains("for"))
    }

    fun testCursorKeywordIsIdentifierOutsideSqlBlock() {
        val tokens = lex("open file;")
        assertTrue(
            "expected 'open' as an identifier outside SQL, got $tokens",
            tokens.any { it.first == RpgTokenTypes.IDENTIFIER && it.second.equals("open", true) },
        )
        assertFalse(
            "no cursor keyword token should appear outside SQL, got $tokens",
            tokens.any { it.first == RpgTokenTypes.SQL_CURSOR_KEYWORD },
        )
    }

    fun testSqlHostVariable() {
        val tokens = lex("exec sql select x into :ws_name from t;")
        assertTrue(
            "expected :ws_name as a host variable, got $tokens",
            tokens.any { it.first == RpgTokenTypes.SQL_HOST_VAR && it.second == ":ws_name" },
        )
    }

    fun testSemicolonInSqlStringDoesNotEndStatement() {
        val tokens = lex("exec sql select ';' as c from sysdummy1;")
        assertTrue(tokens.any { it.first == RpgTokenTypes.SQL_KEYWORD && it.second.equals("from", true) })
    }
}
