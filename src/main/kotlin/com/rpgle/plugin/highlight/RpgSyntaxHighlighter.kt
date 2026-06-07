package com.rpgle.plugin.highlight

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.HighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.TokenType
import com.intellij.psi.tree.IElementType
import com.rpgle.plugin.common.MapSyntaxHighlighter
import com.rpgle.plugin.lexer.RpgLexerAdapter
import com.rpgle.plugin.psi.RpgTokenTypes

class RpgSyntaxHighlighter : MapSyntaxHighlighter() {

    override fun getHighlightingLexer(): Lexer = RpgLexerAdapter()

    // Lexer token → color. DOT (qualified-name separator, e.g. ds.subfield) is
    // intentionally absent so it is left uncolored. The semantic keys below
    // (KEYWORD/OPCODE/PROC_NAME/…) are applied by RpgAnnotator, not the lexer, so
    // they don't appear here.
    override val tokenKeys: Map<IElementType, TextAttributesKey> = mapOf(
        RpgTokenTypes.COMMENT to LINE_COMMENT,
        RpgTokenTypes.STRING to STRING,
        RpgTokenTypes.NUMBER to NUMBER,
        RpgTokenTypes.BIF to BIF,
        RpgTokenTypes.DIRECTIVE to DIRECTIVE,
        RpgTokenTypes.OPERATOR to OPERATOR,
        RpgTokenTypes.SQL_KEYWORD to SQL_KEYWORD,
        RpgTokenTypes.SQL_CURSOR_KEYWORD to SQL_CURSOR_KEYWORD,
        RpgTokenTypes.SQL_HOST_VAR to SQL_HOST_VAR,
        RpgTokenTypes.SEMICOLON to SEPARATOR,
        RpgTokenTypes.COMMA to SEPARATOR,
        RpgTokenTypes.COLON to SEPARATOR,
        RpgTokenTypes.LPAREN to PARENTHESES,
        RpgTokenTypes.RPAREN to PARENTHESES,
        TokenType.BAD_CHARACTER to BAD_CHARACTER,
    )

    companion object {
        val LINE_COMMENT = key("RPG_LINE_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING = key("RPG_STRING", DefaultLanguageHighlighterColors.STRING)
        val NUMBER = key("RPG_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        val BIF = key("RPG_BIF", DefaultLanguageHighlighterColors.STATIC_METHOD)
        val DIRECTIVE = key("RPG_DIRECTIVE", DefaultLanguageHighlighterColors.METADATA)
        val OPERATOR = key("RPG_OPERATOR", DefaultLanguageHighlighterColors.OPERATION_SIGN)
        // Embedded SQL keywords get their own distinct color (teal) rather than
        // sharing the keyword color, so SQL stands out from RPG opcodes. The
        // default color is defined in colorSchemes/RpgDefault.xml (registered via
        // the additionalTextAttributes extension point), so it stays distinct
        // regardless of theme while remaining user-customizable.
        val SQL_KEYWORD = createTextAttributesKey("RPG_SQL_KEYWORD")
        // Cursor definition / lifecycle keywords get a related but distinct color:
        // a slightly darker blue than the SQL teal, so cursor work reads as "SQL,
        // but cursor" rather than blending into the rest of the statement. Default
        // color likewise lives in colorSchemes/RpgDefault.xml.
        val SQL_CURSOR_KEYWORD = createTextAttributesKey("RPG_SQL_CURSOR_KEYWORD")
        val SQL_HOST_VAR = key("RPG_SQL_HOST_VAR", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        val SEPARATOR = key("RPG_SEPARATOR", DefaultLanguageHighlighterColors.SEMICOLON)
        val PARENTHESES = key("RPG_PARENTHESES", DefaultLanguageHighlighterColors.PARENTHESES)
        val BAD_CHARACTER = key("RPG_BAD_CHARACTER", HighlighterColors.BAD_CHARACTER)

        // Semantic keys applied by the annotator (lexer can't classify by word).
        val KEYWORD = key("RPG_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val OPCODE = key("RPG_OPCODE", DefaultLanguageHighlighterColors.KEYWORD)
        val DATA_TYPE = key("RPG_DATA_TYPE", DefaultLanguageHighlighterColors.KEYWORD)
        val PROC_NAME = key("RPG_PROC_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val SUBROUTINE_NAME = key("RPG_SUBROUTINE_NAME", DefaultLanguageHighlighterColors.FUNCTION_DECLARATION)
        val FILE_NAME = key("RPG_FILE_NAME", DefaultLanguageHighlighterColors.INSTANCE_FIELD)
        // A procedure called but declared nowhere in the file — assumed to live
        // in a bound service program. Falls back to FILE_NAME so it defaults to
        // the external-file color, but it's a separate key so users can recolor
        // service-program procedures on their own.
        val SERVICE_PROC = key("RPG_SERVICE_PROC", FILE_NAME)

        private fun key(name: String, fallback: TextAttributesKey) =
            createTextAttributesKey(name, fallback)
    }
}
