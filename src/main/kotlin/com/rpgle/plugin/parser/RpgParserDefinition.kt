package com.rpgle.plugin.parser

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.lexer.RpgLexerAdapter
import com.rpgle.plugin.psi.RpgFile
import com.rpgle.plugin.psi.RpgTokenSets

/**
 * Lightweight parser definition: the lexer does the real work and the parser produces a flat PSI
 * (all tokens as leaf children of the file root), enough for highlighting, completion and the rest.
 */
class RpgParserDefinition : ParserDefinition {

    override fun createLexer(project: Project?): Lexer = RpgLexerAdapter()

    override fun createParser(project: Project?): PsiParser = RpgFlatParser()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun getCommentTokens(): TokenSet = RpgTokenSets.COMMENTS

    override fun getStringLiteralElements(): TokenSet = RpgTokenSets.STRINGS

    override fun getWhitespaceTokens(): TokenSet = RpgTokenSets.WHITESPACES

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    override fun createFile(viewProvider: FileViewProvider): PsiFile = RpgFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(RpgLanguage)
    }
}
