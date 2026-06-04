package com.rpgle.plugin

import com.intellij.openapi.fileTypes.LanguageFileType
import javax.swing.Icon

object RpgFileType : LanguageFileType(RpgLanguage) {
    override fun getName(): String = "RPG"
    override fun getDescription(): String = "IBM RPG / ILE RPG source"
    override fun getDefaultExtension(): String = "rpgle"
    override fun getIcon(): Icon = RpgIcons.FILE
}
