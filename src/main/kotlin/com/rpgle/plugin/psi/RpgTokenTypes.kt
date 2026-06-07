package com.rpgle.plugin.psi

import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.common.FlatTokenType

class RpgTokenType(debugName: String) : FlatTokenType(debugName, RpgLanguage)

/**
 * Token types emitted by the JFlex lexer. Declared with [JvmField] so the
 * generated Java lexer can reference them as `RpgTokenTypes.COMMENT`, etc.
 */
object RpgTokenTypes {
    @JvmField val COMMENT = RpgTokenType("COMMENT")
    @JvmField val DIRECTIVE = RpgTokenType("DIRECTIVE")
    @JvmField val STRING = RpgTokenType("STRING")
    @JvmField val NUMBER = RpgTokenType("NUMBER")
    @JvmField val BIF = RpgTokenType("BIF")
    @JvmField val IDENTIFIER = RpgTokenType("IDENTIFIER")
    @JvmField val SEMICOLON = RpgTokenType("SEMICOLON")
    @JvmField val LPAREN = RpgTokenType("LPAREN")
    @JvmField val RPAREN = RpgTokenType("RPAREN")
    @JvmField val COMMA = RpgTokenType("COMMA")
    @JvmField val COLON = RpgTokenType("COLON")
    @JvmField val DOT = RpgTokenType("DOT")
    @JvmField val OPERATOR = RpgTokenType("OPERATOR")

    // Embedded SQL (inside EXEC SQL ... ; / END-EXEC blocks).
    @JvmField val SQL_KEYWORD = RpgTokenType("SQL_KEYWORD")
    // Cursor definition / lifecycle keywords (DECLARE/CURSOR/OPEN/FETCH/CLOSE),
    // separated from SQL_KEYWORD so they can be colored distinctly.
    @JvmField val SQL_CURSOR_KEYWORD = RpgTokenType("SQL_CURSOR_KEYWORD")
    @JvmField val SQL_HOST_VAR = RpgTokenType("SQL_HOST_VAR")
}
