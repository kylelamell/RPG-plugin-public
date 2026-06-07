package com.rpgle.plugin.dds

import com.rpgle.plugin.common.FlatTokenType

class DdsTokenType(debugName: String) : FlatTokenType(debugName, DdsLanguage)

object DdsTokenTypes {
    @JvmField val COMMENT = DdsTokenType("COMMENT")
    @JvmField val STRING = DdsTokenType("STRING")
    @JvmField val KEYWORD = DdsTokenType("KEYWORD")
    @JvmField val SPECIAL = DdsTokenType("SPECIAL")
    @JvmField val NUMBER = DdsTokenType("NUMBER")
    @JvmField val IDENTIFIER = DdsTokenType("IDENTIFIER")
    @JvmField val LPAREN = DdsTokenType("LPAREN")
    @JvmField val RPAREN = DdsTokenType("RPAREN")
    @JvmField val TEXT = DdsTokenType("TEXT")
}
