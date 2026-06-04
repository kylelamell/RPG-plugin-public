package com.rpgle.plugin

import com.intellij.lang.Language

object RpgLanguage : Language("RPG") {
    private fun readResolve(): Any = RpgLanguage
    override fun isCaseSensitive(): Boolean = false
}
