package com.rpgle.plugin.dds

import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.rpgle.plugin.common.AbstractFlatParserDefinition

class DdsParserDefinition : AbstractFlatParserDefinition(
    TokenSet.create(DdsTokenTypes.COMMENT),
    TokenSet.create(DdsTokenTypes.STRING),
) {

    override fun createLexer(project: Project?): Lexer = DdsLexerAdapter()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = DdsFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(DdsLanguage)
    }
}
