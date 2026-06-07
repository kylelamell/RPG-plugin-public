package com.rpgle.plugin

import com.rpgle.plugin.common.FlatLanguage

object RpgLanguage : FlatLanguage("RPG") {
    private fun readResolve(): Any = RpgLanguage
}
