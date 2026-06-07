package com.rpgle.plugin.common

import com.intellij.lang.Language
import com.intellij.psi.tree.IElementType

/**
 * Base [IElementType] for the plugin's flat languages. The [toString] override
 * gives every token a consistent, debuggable label derived from the concrete
 * subclass (e.g. `"RpgTokenType.COMMENT"`, `"DdsTokenType.KEYWORD"`).
 */
open class FlatTokenType(debugName: String, language: Language) :
    IElementType(debugName, language) {
    override fun toString(): String = javaClass.simpleName + "." + super.toString()
}
