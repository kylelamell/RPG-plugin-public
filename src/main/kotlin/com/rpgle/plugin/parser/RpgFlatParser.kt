package com.rpgle.plugin.parser

import com.intellij.lang.ASTNode
import com.intellij.lang.PsiBuilder
import com.intellij.lang.PsiParser
import com.intellij.psi.tree.IElementType

/** Consumes the whole token stream under the file root. */
class RpgFlatParser : PsiParser {
    override fun parse(root: IElementType, builder: PsiBuilder): ASTNode {
        val mark = builder.mark()
        while (!builder.eof()) {
            builder.advanceLexer()
        }
        mark.done(root)
        return builder.treeBuilt
    }
}
