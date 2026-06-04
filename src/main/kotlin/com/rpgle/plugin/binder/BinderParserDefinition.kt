package com.rpgle.plugin.binder

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.extapi.psi.PsiFileBase
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiBuilder
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

class BinderParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = BinderLexerAdapter()

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

    override fun createFile(viewProvider: FileViewProvider): PsiFile = BinderFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(BinderLanguage)
        private val COMMENTS = TokenSet.create(BinderTokenTypes.COMMENT)
        private val STRINGS = TokenSet.create(BinderTokenTypes.STRING)
    }
}

class BinderFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, BinderLanguage) {
    override fun getFileType(): FileType = BinderFileType
    override fun toString(): String = "RPG Binder Source File"
}
