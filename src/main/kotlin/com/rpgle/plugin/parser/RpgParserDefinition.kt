package com.rpgle.plugin.parser

import com.intellij.lexer.Lexer
import com.intellij.openapi.project.Project
import com.intellij.psi.FileViewProvider
import com.intellij.psi.PsiFile
import com.intellij.psi.tree.IFileElementType
import com.intellij.psi.tree.TokenSet
import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.common.AbstractFlatParserDefinition
import com.rpgle.plugin.lexer.RpgLexerAdapter
import com.rpgle.plugin.psi.RpgFile
import com.rpgle.plugin.psi.RpgTokenTypes

/**
 * Lightweight parser definition. The lexer does the real work; the shared
 * [AbstractFlatParserDefinition] produces a flat PSI (all tokens as leaf children
 * of the file root), which is enough for highlighting, the annotator, completion,
 * commenting and brace matching without a full grammar.
 */
class RpgParserDefinition : AbstractFlatParserDefinition(
    TokenSet.create(RpgTokenTypes.COMMENT),
    TokenSet.create(RpgTokenTypes.STRING),
) {

    override fun createLexer(project: Project?): Lexer = RpgLexerAdapter()

    override fun getFileNodeType(): IFileElementType = FILE

    override fun createFile(viewProvider: FileViewProvider): PsiFile = RpgFile(viewProvider)

    companion object {
        val FILE: IFileElementType = IFileElementType(RpgLanguage)
    }
}
