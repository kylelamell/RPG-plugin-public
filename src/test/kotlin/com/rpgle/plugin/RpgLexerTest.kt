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
        // Legacy fixed-format opcodes Z-ADD / Z-SUB also contain a hyphen and must
        // lex as one token, not IDENTIFIER '-' IDENTIFIER, so the whole word colors
        // as an opcode (not just the "ADD"/"SUB" tail).
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
        // "a-b" is subtraction, not a hyphenated keyword.
        val types = lex("a-b").map { it.first }
        assertTrue(types.contains(RpgTokenTypes.OPERATOR))
    }

    fun testPeriodInQualifiedNameIsDotNotBadCharacter() {
        // A period in a qualified reference (ds.subfield) lexes as DOT, not a
        // BAD_CHARACTER, so it isn't flagged/highlighted as an error.
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
        // The table name is an ordinary identifier, not an SQL keyword.
        assertTrue(tokens.any { it.first == RpgTokenTypes.IDENTIFIER && it.second.equals("custmast", true) })
    }

    fun testSemicolonLeavesSqlState() {
        // The same word ("select") is an SQL keyword inside the block but an
        // ordinary identifier once the statement-ending ';' is consumed.
        val tokens = lex("exec sql select * from t;\nselect = 1;")
        val selects = tokens.filter { it.second.equals("select", true) }
        assertEquals("expected two 'select' words", 2, selects.size)
        assertEquals(RpgTokenTypes.SQL_KEYWORD, selects[0].first)
        assertEquals(RpgTokenTypes.IDENTIFIER, selects[1].first)
    }

    fun testCursorKeywordsAreDistinctFromSqlKeywords() {
        // DECLARE/CURSOR/FETCH lex as SQL_CURSOR_KEYWORD (their own color), while
        // ordinary SQL words in the same statement stay SQL_KEYWORD. FOR is
        // deliberately a general SQL keyword, not a cursor keyword.
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
        // "fetch"/"open"/"close" are RPG opcodes outside SQL; the lexer only
        // emits the SQL cursor token inside an EXEC SQL block.
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

    fun testExecSqlIntroducerIsTwoSeparateSqlKeywords() {
        // EXEC and SQL are separate SQL_KEYWORD tokens (EXEC is recognized with no
        // lookahead, entering a bridge state) rather than one "EXEC SQL" token.
        // This is what lets the editor's incremental re-lexer highlight a freshly
        // typed EXEC SQL block without a reparse — see docs/highlighting.md.
        val tokens = lex("exec sql select 1 from t;").filterNot { it.first == TokenType.WHITE_SPACE }
        assertEquals(RpgTokenTypes.SQL_KEYWORD, tokens[0].first)
        assertEquals("exec", tokens[0].second.lowercase())
        assertEquals(RpgTokenTypes.SQL_KEYWORD, tokens[1].first)
        assertEquals("sql", tokens[1].second.lowercase())
        assertEquals(RpgTokenTypes.SQL_KEYWORD, tokens[2].first)
        assertEquals("select", tokens[2].second.lowercase())
    }

    fun testBareExecNotFollowedBySqlDoesNotSwallowFollowingTokens() {
        // A stray top-level EXEC with no SQL after it must not consume what
        // follows: the bridge state pushes the next character back and re-lexes it
        // normally. (EXEC is harmlessly colored as a keyword — it is not an RPG
        // opcode and appears only in EXEC SQL.)
        val tokens = lex("exec();")
        assertTrue("expected '(' to still tokenize, got $tokens", tokens.any { it.first == RpgTokenTypes.LPAREN })
        assertTrue("expected ')' to still tokenize, got $tokens", tokens.any { it.first == RpgTokenTypes.RPAREN })
        assertTrue("expected ';' to still tokenize, got $tokens", tokens.any { it.first == RpgTokenTypes.SEMICOLON })
    }

    fun testSemicolonInSqlStringDoesNotEndStatement() {
        // A ';' inside a quoted literal must not pop us out of SQL mode, so the
        // trailing FROM is still recognised as a keyword.
        val tokens = lex("exec sql select ';' as c from sysdummy1;")
        assertTrue(tokens.any { it.first == RpgTokenTypes.SQL_KEYWORD && it.second.equals("from", true) })
    }
}
