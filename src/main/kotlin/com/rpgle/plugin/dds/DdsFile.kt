package com.rpgle.plugin.dds

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class DdsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DdsLanguage) {
    override fun getFileType(): FileType = DdsFileType
    override fun toString(): String = "RPG DDS File"
}
