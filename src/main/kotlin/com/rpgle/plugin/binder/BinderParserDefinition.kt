package com.rpgle.plugin.binder

import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.rpgle.plugin.common.AbstractFlatParserDefinition

class BinderParserDefinition : AbstractFlatParserDefinition(
    TokenSet.create(BinderTokenTypes.COMMENT),
    TokenSet.create(BinderTokenTypes.STRING),
) {

    override fun createLexer(project: Project?): Lexer = BinderLexerAdapter()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = BinderFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(BinderLanguage)
    }
}
