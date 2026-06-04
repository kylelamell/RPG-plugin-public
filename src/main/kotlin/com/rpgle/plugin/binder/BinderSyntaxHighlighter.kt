package com.rpgle.plugin.binder

import com.intellij.lexer.Lexer
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.colors.TextAttributesKey.createTextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

class BinderSyntaxHighlighter : SyntaxHighlighterBase() {

    override fun getHighlightingLexer(): Lexer = BinderLexerAdapter()

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        when (tokenType) {
            BinderTokenTypes.COMMENT -> arrayOf(COMMENT)
            BinderTokenTypes.STRING -> arrayOf(STRING)
            BinderTokenTypes.KEYWORD -> arrayOf(KEYWORD)
            BinderTokenTypes.SPECIAL -> arrayOf(SPECIAL)
            BinderTokenTypes.NUMBER -> arrayOf(NUMBER)
            else -> EMPTY
        }

    companion object {
        val COMMENT = createTextAttributesKey("RPG_BND_COMMENT", DefaultLanguageHighlighterColors.BLOCK_COMMENT)
        val STRING = createTextAttributesKey("RPG_BND_STRING", DefaultLanguageHighlighterColors.STRING)
        val KEYWORD = createTextAttributesKey("RPG_BND_KEYWORD", DefaultLanguageHighlighterColors.KEYWORD)
        val SPECIAL = createTextAttributesKey("RPG_BND_SPECIAL", DefaultLanguageHighlighterColors.CONSTANT)
        val NUMBER = createTextAttributesKey("RPG_BND_NUMBER", DefaultLanguageHighlighterColors.NUMBER)
        private val EMPTY = emptyArray<TextAttributesKey>()
    }
}
