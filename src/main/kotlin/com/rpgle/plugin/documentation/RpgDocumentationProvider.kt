package com.rpgle.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.psi.RpgTokenTypes
import com.rpgle.plugin.scan.RpgSymbol
import com.rpgle.plugin.scan.RpgSymbolScanner

/**
 * Hover / quick documentation for RPG symbols declared in the file:
 *  - **variables** show their declared type,
 *  - **constants** show their value,
 *  - **procedures / prototypes** show their parameters (name + type) and return
 *    type.
 *
 * The PSI is flat (every token is a leaf), so there are no references to resolve.
 * [getCustomDocumentationElement] hands the platform the identifier leaf under the
 * caret as the documentation target whenever it names a known symbol, and
 * [generateDoc] looks that name up in the symbol scan.
 */
class RpgDocumentationProvider : AbstractDocumentationProvider() {

    override fun getCustomDocumentationElement(
        editor: Editor,
        file: PsiFile,
        contextElement: PsiElement?,
        targetOffset: Int,
    ): PsiElement? {
        if (file.language != RpgLanguage) return null
        if (!isIdentifier(contextElement)) return null
        return if (resolve(contextElement!!) != null) contextElement else null
    }

    override fun generateDoc(element: PsiElement?, originalElement: PsiElement?): String? {
        val target = element ?: return null
        if (!isIdentifier(target)) return null
        val symbol = resolve(target) ?: return null
        return render(symbol)
    }

    override fun getQuickNavigateInfo(element: PsiElement?, originalElement: PsiElement?): String? {
        val target = element ?: return null
        if (!isIdentifier(target)) return null
        val symbol = resolve(target) ?: return null
        return StringUtil.escapeXmlEntities(signature(symbol))
    }

    private fun isIdentifier(element: PsiElement?): Boolean =
        element?.node?.elementType == RpgTokenTypes.IDENTIFIER

    /** Resolves the identifier under [element] to a symbol declared in its file. */
    private fun resolve(element: PsiElement): RpgSymbol? {
        val file = element.containingFile ?: return null
        return findSymbol(file, element.text)
    }

    private fun findSymbol(file: PsiFile, name: String): RpgSymbol? =
        RpgSymbolScanner.scan(file).symbols
            .filter { it.name.equals(name, ignoreCase = true) }
            // Prefer the match carrying the most documentation detail (e.g. the
            // implementation's DCL-PI over a bare name, or a typed declaration).
            .maxByOrNull(::detailScore)

    private fun detailScore(symbol: RpgSymbol): Int {
        var score = 0
        if (symbol.parameters != null) score += 2
        if (symbol.typeText != null) score += 1
        if (symbol.value != null) score += 1
        return score
    }

    // --- Rendering -----------------------------------------------------------

    private fun render(symbol: RpgSymbol): String {
        val sb = StringBuilder()
        sb.append(DocumentationMarkup.DEFINITION_START)
        sb.append(symbol.kind.label).append(' ').append(code(symbol.name))
        sb.append(DocumentationMarkup.DEFINITION_END)

        val sections = StringBuilder()
        when (symbol.kind) {
            RpgSymbol.Kind.VARIABLE ->
                symbol.typeText?.let { section(sections, "Type", code(esc(it))) }

            RpgSymbol.Kind.CONSTANT ->
                symbol.value?.let { section(sections, "Value", code(esc(it))) }

            RpgSymbol.Kind.PROCEDURE, RpgSymbol.Kind.PROTOTYPE -> {
                val params = symbol.parameters
                if (!params.isNullOrEmpty()) {
                    section(sections, "Parameters", params.joinToString("<br/>") { param ->
                        code(esc(param.name)) +
                                (param.type?.let { " " + grayed(esc(it)) } ?: "")
                    })
                } else if (params != null) {
                    section(sections, "Parameters", grayed("none"))
                }
                section(
                    sections,
                    "Returns",
                    symbol.typeText?.let { code(esc(it)) } ?: grayed("nothing"),
                )
            }

            else -> {}
        }
        symbol.sourceFile?.let { section(sections, "Defined in", esc(it)) }

        if (sections.isNotEmpty()) {
            sb.append(DocumentationMarkup.SECTIONS_START)
            sb.append(sections)
            sb.append(DocumentationMarkup.SECTIONS_END)
        }
        return sb.toString()
    }

    private fun section(sb: StringBuilder, header: String, content: String) {
        sb.append(DocumentationMarkup.SECTION_HEADER_START)
            .append(header).append(':')
            .append(DocumentationMarkup.SECTION_SEPARATOR)
            .append(content)
            .append(DocumentationMarkup.SECTION_END)
            .append("</tr>")
    }

    /** A compact one-line signature, used for the Ctrl-hover navigate tooltip. */
    private fun signature(symbol: RpgSymbol): String = when (symbol.kind) {
        RpgSymbol.Kind.VARIABLE ->
            symbol.name + (symbol.typeText?.let { " : $it" } ?: "")

        RpgSymbol.Kind.CONSTANT ->
            symbol.name + (symbol.value?.let { " = $it" } ?: "")

        RpgSymbol.Kind.PROCEDURE, RpgSymbol.Kind.PROTOTYPE -> {
            val params = symbol.parameters.orEmpty()
                .joinToString("; ") { p -> p.name + (p.type?.let { " $it" } ?: "") }
            symbol.name + "(" + params + ")" + (symbol.typeText?.let { " : $it" } ?: "")
        }

        else -> "${symbol.kind.label} ${symbol.name}"
    }

    private fun esc(s: String): String = StringUtil.escapeXmlEntities(s)
    private fun code(s: String): String = "<code>$s</code>"
    private fun grayed(s: String): String =
        DocumentationMarkup.GRAYED_START + s + DocumentationMarkup.GRAYED_END
}
