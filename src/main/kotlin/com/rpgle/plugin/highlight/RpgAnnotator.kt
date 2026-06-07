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
 * Semantic colouring that the lexer-based highlighter can't do, because it
 * requires word lookups (keywords / opcodes / data types) and a little context
 * (the name token right after a declaration keyword).
 */
class RpgAnnotator : Annotator {

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (element.node?.elementType != RpgTokenTypes.IDENTIFIER) return
        val upper = element.text.uppercase()
        val file = element.containingFile ?: return

        // Inside an EXEC SQL block an identifier is a table / column / function
        // name, not an RPG symbol or keyword — leave coloring there to the lexer's
        // SQL tokens. Checked first (before the keyword/opcode/data-type lookup) so
        // an SQL identifier that happens to match an RPG word — a column named
        // DATE, TIME, READ, … — is not miscolored as an RPG keyword. The cheap
        // hasEmbeddedSql guard keeps SQL-free files from paying for the scan.
        if (RpgSqlPresence.hasEmbeddedSql(file) && isInsideSql(element)) return

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

        // Declaration/definition context: the name token right after a
        // declaration keyword (DCL-PROC name, DCL-F name, BEGSR/EXSR name).
        val prev = prevMeaningful(element)
        if (prev?.node?.elementType == RpgTokenTypes.IDENTIFIER) {
            val contextKey = when (prev.text.uppercase()) {
                "DCL-PROC", "DCL-PR", "DCL-PI" -> RpgSyntaxHighlighter.PROC_NAME
                "DCL-F" -> RpgSyntaxHighlighter.FILE_NAME
                // Subroutine name at its declaration (BEGSR name) and at call
                // sites (EXSR name): a plain IDENTIFIER the lexer can't classify.
                "BEGSR", "EXSR" -> RpgSyntaxHighlighter.SUBROUTINE_NAME
                else -> null
            }
            if (contextKey != null) {
                apply(holder, element, contextKey)
                return
            }
        }

        // Use-site coloring.
        // A procedure declared in this file, colored where it is *called* (the
        // lexer only sees the declaration). Only the symbol scan knows the name.
        if (upper in RpgLocalSymbols.procedureNames(file)) {
            apply(holder, element, RpgSyntaxHighlighter.PROC_NAME)
            return
        }
        // A procedure *call* whose name is declared nowhere in this file is
        // assumed to live in a bound service program. With no local declaration
        // to learn the name from, the call syntax is the only signal, so we key
        // off an identifier immediately followed by '(' — a bare undeclared
        // identifier is left alone, and so is a locally-declared non-procedure
        // indexed with '(' (e.g. an array). It gets the service-program color,
        // which defaults to the external-file color but is separately
        // customizable.
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
     * Whether [element] sits inside an `EXEC SQL … ;` block. Scans backward to
     * the nearest decisive token: any SQL token (the block opener, a keyword, a
     * cursor keyword, or a host variable) means inside; a statement terminator
     * (`;` or `END-EXEC`) or the start of file means outside. SQL tokens only
     * occur inside SQL blocks, and every block ends with `;`, so the first
     * decisive token behind the element settles the question.
     */
    private fun isInsideSql(element: PsiElement): Boolean {
        var prev = PsiTreeUtil.prevLeaf(element, true)
        while (prev != null) {
            when (prev.node?.elementType) {
                TokenType.WHITE_SPACE, RpgTokenTypes.COMMENT -> {}
                RpgTokenTypes.SEMICOLON -> return false
                RpgTokenTypes.SQL_CURSOR_KEYWORD, RpgTokenTypes.SQL_HOST_VAR -> return true
                RpgTokenTypes.SQL_KEYWORD ->
                    // The block opener ("EXEC SQL") and inner keywords mean we're
                    // inside; the "/END-EXEC" terminator means we're past a block.
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
