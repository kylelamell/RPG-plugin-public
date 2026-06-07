package com.rpgle.plugin.common

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/**
 * Consumes the whole token stream under the file root, producing a flat PSI (all
 * tokens as leaf children of the file element). Shared by every flat language in
 * the plugin (RPG, Binder, DDS) — the lexer does the real work, so no language
 * needs a grammar-driven parser.
 */
class FlatPsiParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mark = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        mark.done(root)
        return builder.treeBuilt
    }
}
