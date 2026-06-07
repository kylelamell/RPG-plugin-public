package com.rpgle.plugin.common

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighterBase
import com.intellij.psi.tree.IElementType

/**
 * Shared syntax highlighter for the plugin's flat languages: maps each lexer
 * token type to at most one [TextAttributesKey] via [tokenKeys]. Subclasses
 * supply the highlighting lexer and the token→key map; the key *definitions*
 * (their external names and default colors) stay per-language.
 *
 * The single-key arrays are wrapped once and reused (no per-call allocation), so
 * this is as cheap as the hand-rolled per-token arrays it replaces.
 */
abstract class MapSyntaxHighlighter : SyntaxHighlighterBase() {

    /** Token type → its color. Tokens absent from the map are left uncolored. */
    protected abstract val tokenKeys: Map<IElementType, TextAttributesKey>

    private val packed: Map<IElementType, Array<TextAttributesKey>> by lazy {
        tokenKeys.mapValues { (_, key) -> arrayOf(key) }
    }

    override fun getTokenHighlights(tokenType: IElementType): Array<TextAttributesKey> =
        packed[tokenType] ?: EMPTY

    companion object {
        private val EMPTY = emptyArray<TextAttributesKey>()
    }
}
