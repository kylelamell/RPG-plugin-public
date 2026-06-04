package com.rpgle.plugin.dds

import com.intellij.lang.Language

object DdsLanguage : Language("RPGDDS") {
    private fun readResolve(): Any = DdsLanguage
    override fun isCaseSensitive(): Boolean = false
}
