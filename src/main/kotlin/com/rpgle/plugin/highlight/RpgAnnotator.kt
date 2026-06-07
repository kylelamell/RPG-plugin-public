package com.rpgle.plugin.highlight

import com.intellij.lang.annotation.AnnotationHolder
import com.intellij.lang.annotation.Annotator
import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.psi.PsiElement
import com.intellij.psi.TokenType
import com.intellij.psi.util.PsiTreeUtil
import com.rpgle.plugin.data.RpgWords
import com.rpgle.plugin.psi.RpgTokenTypes
import com.rpgle.plugin.scan.RpgLocalSymbols
import com.rpgle.plugin.scan.RpgSqlPresence

/**
 * Semantic coloring the lexer-based highlighter can't do: it needs word lookups
 * (keywords / opcodes / data types) and a little context after a declaration keyword.
 */
class RpgAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.node?.elementType != RpgTokenTypes.IDENTIFIER) return
        val upper = element.text.uppercase()
        val file = element.containingFile ?: return

        val keywordKey = when (upper) {
            in RpgWords.OPCODES -> RpgSyntaxHighlighter.OPCODE
            in RpgWords.DECL_KEYWORDS -> RpgSyntaxHighlighter.KEYWORD
            in RpgWords.DATA_TYPES -> RpgSyntaxHighlighter.DATA_TYPE
            in RpgWords.KEYWORD_ARGS -> RpgSyntaxHighlighter.KEYWORD
            else -> null
        }
        if (keywordKey != null) {
            apply(holder, element, keywordKey)
            return
        }

        val prev = prevMeaningful(element)
        if (prev?.node?.elementType == RpgTokenTypes.IDENTIFIER) {
            val contextKey = when (prev.text.uppercase()) {
                "DCL-PROC", "DCL-PR", "DCL-PI" -> RpgSyntaxHighlighter.PROC_NAME
                "DCL-F" -> RpgSyntaxHighlighter.FILE_NAME
                "BEGSR", "EXSR" -> RpgSyntaxHighlighter.SUBROUTINE_NAME
                else -> null
            }
            if (contextKey != null) {
                apply(holder, element, contextKey)
                return
            }
        }

        if (isInsideSql(element)) return

        val file = element.containingFile
        if (upper in RpgLocalSymbols.procedureNames(file)) {
            apply(holder, element, RpgSyntaxHighlighter.PROC_NAME)
            return
        }
        if (isCallSite(element) && upper !in RpgLocalSymbols.declaredNames(file)) {
            apply(holder, element, RpgSyntaxHighlighter.SERVICE_PROC)
        }
    }

    private fun apply(holder: AnnotationHolder, element: PsiElement, key: TextAttributesKey) {
        holder.newSilentAnnotation(HighlightSeverity.INFORMATION)
            .range(element)
            .textAttributes(key)
            .create()
    }

    /** True when [element] is immediately followed by '(' — i.e. used as a call. */
    private fun isCallSite(element: PsiElement): Boolean =
        nextMeaningful(element)?.node?.elementType == RpgTokenTypes.LPAREN

    /**
     * Whether [element] sits inside an `EXEC SQL … ;` block, decided by scanning back to
     * the nearest decisive token: an SQL token means inside, a `;` / `END-EXEC` means outside.
     */
    private fun isInsideSql(element: PsiElement): Boolean {
        var prev = PsiTreeUtil.prevLeaf(element, true)
        while (prev != null) {
            when (prev.node?.elementType) {
                TokenType.WHITE_SPACE, RpgTokenTypes.COMMENT -> {}
                RpgTokenTypes.SEMICOLON -> return false
                RpgTokenTypes.SQL_CURSOR_KEYWORD, RpgTokenTypes.SQL_HOST_VAR -> return true
                RpgTokenTypes.SQL_KEYWORD ->
                    return prev.text.trim().uppercase().removePrefix("/") != "END-EXEC"
                else -> {}
            }
            prev = PsiTreeUtil.prevLeaf(prev, true)
        }
        return false
    }

    private fun prevMeaningful(element: PsiElement): PsiElement? {
        var prev = PsiTreeUtil.prevLeaf(element, true)
        while (prev != null) {
            val type = prev.node?.elementType
            if (type != TokenType.WHITE_SPACE && type != RpgTokenTypes.COMMENT) return prev
            prev = PsiTreeUtil.prevLeaf(prev, true)
        }
        return null
    }

    private fun nextMeaningful(element: PsiElement): PsiElement? {
        var next = PsiTreeUtil.nextLeaf(element, true)
        while (next != null) {
            val type = next.node?.elementType
            if (type != TokenType.WHITE_SPACE && type != RpgTokenTypes.COMMENT) return next
            next = PsiTreeUtil.nextLeaf(next, true)
        }
        return null
    }
}
