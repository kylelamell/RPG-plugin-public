package com.rpgle.plugin.psi

import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

object RpgTokenSets {
    val COMMENTS: TokenSet = TokenSet.create(RpgTokenTypes.COMMENT)
    val STRINGS: TokenSet = TokenSet.create(RpgTokenTypes.STRING)
    val WHITESPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
}
