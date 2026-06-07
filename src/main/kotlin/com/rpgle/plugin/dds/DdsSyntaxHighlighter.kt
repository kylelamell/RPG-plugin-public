package com.rpgle.plugin.dds

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.tree.IElementType
import com.rpgle.plugin.common.MapSyntaxHighlighter

class DdsSyntaxHighlighter : MapSyntaxHighlighter() {

    override fun getHighlightingLexer(): Lexer = DdsLexerAdapter()

    override val tokenKeys: Map<IElementType, TextAttributesKey> = mapOf(
        DdsTokenTypes.COMMENT to COMMENT,
        DdsTokenTypes.STRING to STRING,
        DdsTokenTypes.KEYWORD to KEYWORD,
        DdsTokenTypes.SPECIAL to SPECIAL,
        DdsTokenTypes.NUMBER to NUMBER,
    )

    companion object {
        val COMMENT = createTextAttributesKey("RPG_DDS_COMMENT", DefaultLanguageHighlighterColors.LINE_COMMENT)
        val STRING = createTextAttributesKey("RPG_DDS_STRING", DefaultLanguageHighlighterColors.STRING)
        val KEYWORD = createTextAttributesKey("RPG_DDS_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val SPECIAL = createTextAttributesKey("RPG_DDS_SPECIAL", DefaultLanguageHighlighterColors.CONSTANT)
        val NUMBER = createTextAttributesKey("RPG_DDS_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    }
}
