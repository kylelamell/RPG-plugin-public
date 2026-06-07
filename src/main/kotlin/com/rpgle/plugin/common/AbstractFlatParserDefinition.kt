package com.rpgle.plugin.common

import com.intellij.extapi.psi.ASTWrapperPsiElement
import com.intellij.lang.ASTNode
import com.intellij.lang.ParserDefinition
import com.intellij.lang.PsiParser
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.tree.TokenSet

/**
 * Shared [ParserDefinition] for the plugin's flat languages. Supplies everything
 * common to RPG / Binder / DDS: a [FlatPsiParser], the comment / string token
 * sets (passed in), the standard whitespace set, and a leaf [PsiElement] wrapper.
 *
 * Subclasses provide only what is genuinely language-specific: the lexer
 * ([createLexer]), the file node type ([getFileNodeType]) and the PSI file
 * element ([createFile]).
 */
abstract class AbstractFlatParserDefinition(
    private val comments: TokenSet,
    private val strings: TokenSet,
) : ParserDefinition {

    override fun createParser(project: Project?): PsiParser = FlatPsiParser()

    override fun getCommentTokens(): TokenSet = comments

    override fun getStringLiteralElements(): TokenSet = strings

    override fun getWhitespaceTokens(): TokenSet = WHITESPACES

    override fun createElement(node: ASTNode): PsiElement = ASTWrapperPsiElement(node)

    companion object {
        private val WHITESPACES: TokenSet = TokenSet.create(TokenType.WHITE_SPACE)
    }
}
