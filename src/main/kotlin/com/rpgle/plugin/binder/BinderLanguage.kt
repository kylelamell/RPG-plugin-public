package com.rpgle.plugin.binder

import com.rpgle.plugin.common.FlatLanguage

object BinderLanguage : FlatLanguage("RPGBinder") {
    private fun readResolve(): Any = BinderLanguage
}
