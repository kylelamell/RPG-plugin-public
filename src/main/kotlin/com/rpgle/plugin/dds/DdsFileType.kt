package com.rpgle.plugin.dds

import com.intellij.openapi.fileTypes.LanguageFileType
import com.rpgle.plugin.RpgIcons
import javax.swing.Icon

object DdsFileType : LanguageFileType(DdsLanguage) {
    override fun getName(): String = "RPG DDS"
    override fun getDescription(): String = "DDS physical / logical file source"
    override fun getDefaultExtension(): String = "pf"
    override fun getIcon(): Icon = RpgIcons.FILE
}
