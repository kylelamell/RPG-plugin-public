package com.rpgle.plugin.dds

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IElementType
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet

class DdsParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = DdsLexerAdapter()

    override fun createParser(project: Project?): PsiParser = PsiParser { root, builder ->
        val mark = builder.mark()
        while (!builder.eof()) builder.advanceLexer()
        mark.done(root)
        builder.treeBuilt
    }

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = COMMENTS

    override fun getStringLiteralElements(): TokenSet = STRINGS

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = DdsFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(DdsLanguage)
        private val COMMENTS = TokenSet.create(DdsTokenTypes.COMMENT)
        private val STRINGS = TokenSet.create(DdsTokenTypes.STRING)
    }
}

class DdsFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, DdsLanguage) {
    override fun getFileType(): FileType = DdsFileType
    override fun toString(): String = "RPG DDS File"
}
