package com.rpgle.plugin.documentation

import com.intellij.lang.documentation.AbstractDocumentationProvider
import com.intellij.lang.documentation.DocumentationMarkup
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.util.text.StringUtil
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.psi.RpgTokenTypes
import com.rpgle.plugin.scan.RpgIncludeResolver
import com.rpgle.plugin.scan.RpgSymbol
import com.rpgle.plugin.scan.RpgSymbolScanner

/**
 * Hover / quick documentation for RPG symbols declared in the file or reached via `/COPY`,
 * showing a variable's type, a constant's value, or a procedure's parameters and return type.
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

    /** Resolves the identifier under [element] to a symbol, local first then /COPY. */
    private fun resolve(element: PsiElement): RpgSymbol? {
        val file = element.containingFile ?: return null
        return findSymbol(file, element.text)
    }

    private fun findSymbol(file: PsiFile, name: String): RpgSymbol? {
        val scan = RpgSymbolScanner.scan(file)
        val matches = ArrayList<RpgSymbol>()
        scan.symbols.filterTo(matches) { it.name.equals(name, ignoreCase = true) }

        val psiManager = PsiManager.getInstance(file.project)
        val visited = HashSet<String>()
        for (include in scan.includes) {
            for (virtualFile in RpgIncludeResolver.resolve(include, file)) {
                if (!visited.add(virtualFile.url)) continue
                val includedPsi = psiManager.findFile(virtualFile) ?: continue
                RpgSymbolScanner.scan(includedPsi).symbols
                    .filterTo(matches) { it.name.equals(name, ignoreCase = true) }
            }
        }

        return matches.maxByOrNull(::detailScore)
    }

    private fun detailScore(symbol: RpgSymbol): Int {
        var score = 0
        if (symbol.parameters != null) score += 2
        if (symbol.typeText != null) score += 1
        if (symbol.value != null) score += 1
        return score
    }

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
