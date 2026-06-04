package com.rpgle.plugin.binder

import com.intellij.lang.Language

object BinderLanguage : Language("RPGBinder") {
    private fun readResolve(): Any = BinderLanguage
    override fun isCaseSensitive(): Boolean = false
}
