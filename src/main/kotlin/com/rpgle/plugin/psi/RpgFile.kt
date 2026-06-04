package com.rpgle.plugin.psi

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import com.rpgle.plugin.RpgFileType
import com.rpgle.plugin.RpgLanguage

class RpgFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, RpgLanguage) {
    override fun getFileType(): FileType = RpgFileType
    override fun toString(): String = "RPG File"
}
