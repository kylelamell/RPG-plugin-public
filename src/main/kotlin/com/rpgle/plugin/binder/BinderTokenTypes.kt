package com.rpgle.plugin.binder

import com.intellij.psi.tree.IElementType

class BinderTokenType(debugName: String) : IElementType(debugName, BinderLanguage)

object BinderTokenTypes {
    @JvmField val COMMENT = BinderTokenType("COMMENT")
    @JvmField val STRING = BinderTokenType("STRING")
    @JvmField val KEYWORD = BinderTokenType("KEYWORD")
    @JvmField val SPECIAL = BinderTokenType("SPECIAL")
    @JvmField val IDENTIFIER = BinderTokenType("IDENTIFIER")
    @JvmField val NUMBER = BinderTokenType("NUMBER")
    @JvmField val LPAREN = BinderTokenType("LPAREN")
    @JvmField val RPAREN = BinderTokenType("RPAREN")
    @JvmField val OPERATOR = BinderTokenType("OPERATOR")
}
