package com.rpgle.plugin.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rpgle.plugin.lexer.RpgLexerAdapter
import com.rpgle.plugin.psi.RpgTokenTypes

class RpgSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = RpgLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        when (tokenType) {
            RpgTokenTypes.COMMENT -> COMMENT_KEYS
            RpgTokenTypes.STRING -> STRING_KEYS
            RpgTokenTypes.NUMBER -> NUMBER_KEYS
            RpgTokenTypes.BIF -> BIF_KEYS
            RpgTokenTypes.DIRECTIVE -> DIRECTIVE_KEYS
            RpgTokenTypes.OPERATOR -> OPERATOR_KEYS
            RpgTokenTypes.SQL_KEYWORD -> SQL_KEYWORD_KEYS
            RpgTokenTypes.SQL_CURSOR_KEYWORD -> SQL_CURSOR_KEYWORD_KEYS
            RpgTokenTypes.SQL_HOST_VAR -> SQL_HOST_VAR_KEYS
            RpgTokenTypes.SEMICOLON,
            RpgTokenTypes.COMMA,
            RpgTokenTypes.COLON -> SEPARATOR_KEYS
            RpgTokenTypes.LPAREN,
            RpgTokenTypes.RPAREN -> PAREN_KEYS
            TokenType.BAD_CHARACTER -> BAD_CHAR_KEYS
            else -> EMPTY
        }

    companion object {
        val LINE_COMMENT = key("RPG_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING = key("RPG_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER = key("RPG_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val BIF = key("RPG_BIF", DefaultLanguageHighlighterColors.STATIC_METHOD)
        val DIRECTIVE = key("RPG_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA)
        val OPERATOR = key("RPG_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        val SQL_KEYWORD = createTextAttributesKey("RPG_SQL_KEYWORD")
        val SQL_CURSOR_KEYWORD = createTextAttributesKey("RPG_SQL_CURSOR_KEYWORD")
        val SQL_HOST_VAR = key("RPG_SQL_HOST_VAR", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val SEPARATOR = key("RPG_SEPARATOR", DefaultLanguageHighlighterColors.SEMICOLON)
        val PARENTHESES = key("RPG_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BAD_CHARACTER = key("RPG_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        val KEYWORD = key("RPG_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val OPCODE = key("RPG_OPCODE", DefaultLanguageHighlighterColors.KEYWORD)
        val DATA_TYPE = key("RPG_DATA_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
        val PROC_NAME = key("RPG_PROC_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val SUBROUTINE_NAME = key("RPG_SUBROUTINE_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val FILE_NAME = key("RPG_FILE_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val SERVICE_PROC = key("RPG_SERVICE_PROC", FILE_NAME)

        private fun key(name: String, fallback: TextAttributesKey) =
            createTextAttributesKey(name, fallback)

        private val COMMENT_KEYS = arrayOf(LINE_COMMENT)
        private val STRING_KEYS = arrayOf(STRING)
        private val NUMBER_KEYS = arrayOf(NUMBER)
        private val BIF_KEYS = arrayOf(BIF)
        private val DIRECTIVE_KEYS = arrayOf(DIRECTIVE)
        private val OPERATOR_KEYS = arrayOf(OPERATOR)
        private val SQL_KEYWORD_KEYS = arrayOf(SQL_KEYWORD)
        private val SQL_CURSOR_KEYWORD_KEYS = arrayOf(SQL_CURSOR_KEYWORD)
        private val SQL_HOST_VAR_KEYS = arrayOf(SQL_HOST_VAR)
        private val SEPARATOR_KEYS = arrayOf(SEPARATOR)
        private val PAREN_KEYS = arrayOf(PARENTHESES)
        private val BAD_CHAR_KEYS = arrayOf(BAD_CHARACTER)
        private val EMPTY = emptyArray<TextAttributesKey>()
    }
}
