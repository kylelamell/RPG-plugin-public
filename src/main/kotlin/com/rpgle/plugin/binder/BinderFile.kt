package com.rpgle.plugin.binder

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider

class BinderFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, BinderLanguage) {
    override fun getFileType(): FileType = BinderFileType
    override fun toString(): String = "RPG Binder Source File"
}
