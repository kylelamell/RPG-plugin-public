package com.rpgle.plugin.dds

import com.rpgle.plugin.common.FlatLanguage

object DdsLanguage : FlatLanguage("RPGDDS") {
    private fun readResolve(): Any = DdsLanguage
}
