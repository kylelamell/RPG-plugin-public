package com.rpgle.plugin.dds

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class DdsSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = DdsLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        when (tokenType) {
            DdsTokenTypes.COMMENT -> arrayOf(COMMENT)
            DdsTokenTypes.STRING -> arrayOf(STRING)
            DdsTokenTypes.KEYWORD -> arrayOf(KEYWORD)
            DdsTokenTypes.SPECIAL -> arrayOf(SPECIAL)
            DdsTokenTypes.NUMBER -> arrayOf(NUMBER)
            else -> EMPTY
        }

    companion object {
        val COMMENT = createTextAttributesKey("RPG_DDS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING = createTextAttributesKey("RPG_DDS_STRING", DefaultLanguageHighlighterColors.STRING)
        val KEYWORD = createTextAttributesKey("RPG_DDS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val SPECIAL = createTextAttributesKey("RPG_DDS_SPECIAL", DefaultLanguageHighlighterColors.CONSTANT)
        val NUMBER = createTextAttributesKey("RPG_DDS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        private val EMPTY = emptyArray<TextAttributesKey>()
    }
}
