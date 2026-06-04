package com.rpgle.plugin.psi

import com.intellij.psi.tree.IElementType
import com.rpgle.plugin.RpgLanguage

class RpgTokenType(debugName: String) : IElementType(debugName, RpgLanguage) {
    override fun toString(): String = "RpgTokenType." + super.toString()
}

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

    @JvmField val SQL_KEYWORD = RpgTokenType("SQL_KEYWORD")
    @JvmField val SQL_CURSOR_KEYWORD = RpgTokenType("SQL_CURSOR_KEYWORD")
    @JvmField val SQL_HOST_VAR = RpgTokenType("SQL_HOST_VAR")
}
