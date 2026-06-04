package com.rpgle.plugin.binder

import com.intellij.openapi.fileTypes.LanguageFileType
import com.rpgle.plugin.RpgIcons
import javax.swing.Icon

object BinderFileType : LanguageFileType(BinderLanguage) {
    override fun getName(): String = "RPG Binder Source"
    override fun getDescription(): String = "Service program binder / export source"
    override fun getDefaultExtension(): String = "bnd"
    override fun getIcon(): Icon = RpgIcons.FILE
}
