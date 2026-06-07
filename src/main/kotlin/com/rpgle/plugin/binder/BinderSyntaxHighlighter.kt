package com.rpgle.plugin.binder

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.psi.tree.IElementType
import com.rpgle.plugin.common.MapSyntaxHighlighter

class BinderSyntaxHighlighter : MapSyntaxHighlighter() {

    override fun getHighlightingLexer(): Lexer = BinderLexerAdapter()

    override val tokenKeys: Map<IElementType, TextAttributesKey> = mapOf(
        BinderTokenTypes.COMMENT to COMMENT,
        BinderTokenTypes.STRING to STRING,
        BinderTokenTypes.KEYWORD to KEYWORD,
        BinderTokenTypes.SPECIAL to SPECIAL,
        BinderTokenTypes.NUMBER to NUMBER,
    )

    companion object {
        val COMMENT = createTextAttributesKey("RPG_BND_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val STRING = createTextAttributesKey("RPG_BND_STRING", DefaultLanguageHighlighterColors.STRING)
        val KEYWORD = createTextAttributesKey("RPG_BND_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val SPECIAL = createTextAttributesKey("RPG_BND_SPECIAL", DefaultLanguageHighlighterColors.CONSTANT)
        val NUMBER = createTextAttributesKey("RPG_BND_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
    }
}
