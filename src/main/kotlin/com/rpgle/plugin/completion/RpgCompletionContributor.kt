package com.rpgle.plugin.completion

import com.intellij.codeInsight.completion.CompletionContributor
import com.intellij.codeInsight.completion.CompletionParameters
import com.intellij.codeInsight.completion.CompletionProvider
import com.intellij.codeInsight.completion.CompletionResultSet
import com.intellij.codeInsight.completion.CompletionType
import com.intellij.codeInsight.lookup.LookupElementBuilder
import com.intellij.icons.AllIcons
import com.intellij.patterns.PlatformPatterns
import com.intellij.psi.PsiManager
import com.intellij.util.ProcessingContext
import com.rpgle.plugin.RpgLanguage
import com.rpgle.plugin.data.RpgWords
import com.rpgle.plugin.scan.RpgIncludeResolver
import com.rpgle.plugin.scan.RpgSymbol
import com.rpgle.plugin.scan.RpgSymbolScanner
import javax.swing.Icon

class RpgCompletionContributor : CompletionContributor() {

    init {
        extend(
            CompletionType.BASIC,
            PlatformPatterns.psiElement().withLanguage(RpgLanguage),
            RpgCompletionProvider(),
        )
    }

    private class RpgCompletionProvider : CompletionProvider<CompletionParameters>() {

        override fun addCompletions(
            parameters: CompletionParameters,
            context: ProcessingContext,
            result: CompletionResultSet,
        ) {
            val file = parameters.originalFile
            if (file.language != RpgLanguage) return

            val ci = result.caseInsensitive()

            addBifs(parameters, ci)

            for (keyword in RpgWords.COMPLETION_KEYWORDS) {
                ci.addElement(
                    LookupElementBuilder.create(keyword)
                        .withTypeText("keyword")
                        .withCaseSensitivity(false)
                )
            }

            val local = RpgSymbolScanner.scan(file)
            for (symbol in local.symbols) {
                ci.addElement(lookupFor(symbol.name, symbol.kind.label, iconFor(symbol.kind)))
            }

            addIncludedPrototypes(file, local.includes, ci)
        }

        /** BIFs start with '%', which is not an identifier char, so widen the
         *  prefix to include a leading '%' the user already typed. */
        private fun addBifs(parameters: CompletionParameters, result: CompletionResultSet) {
            val prefix = result.prefixMatcher.prefix
            val doc = parameters.editor.document.immutableCharSequence
            val prefixStart = parameters.offset - prefix.length
            val target = if (prefixStart > 0 && doc[prefixStart - 1] == '%') {
                result.withPrefixMatcher("%$prefix")
            } else {
                result
            }
            for (bif in RpgWords.BIFS) {
                target.addElement(lookupFor(bif, "BIF", AllIcons.Nodes.Method))
            }
        }

        private fun addIncludedPrototypes(
            file: com.intellij.psi.PsiFile,
            includes: List<String>,
            result: CompletionResultSet,
        ) {
            val psiManager = PsiManager.getInstance(file.project)
            val visited = HashSet<String>()
            for (include in includes) {
                for (virtualFile in RpgIncludeResolver.resolve(include, file)) {
                    if (!visited.add(virtualFile.url)) continue
                    val includedPsi = psiManager.findFile(virtualFile) ?: continue
                    val scan = RpgSymbolScanner.scan(includedPsi)
                    for (symbol in scan.symbols) {
                        if (symbol.kind == RpgSymbol.Kind.PROTOTYPE ||
                            symbol.kind == RpgSymbol.Kind.PROCEDURE
                        ) {
                            result.addElement(
                                lookupFor(symbol.name, "proc (${virtualFile.name})", AllIcons.Nodes.Method)
                            )
                        }
                    }
                }
            }
        }

        private fun lookupFor(name: String, typeText: String, icon: Icon) =
            LookupElementBuilder.create(name)
                .withIcon(icon)
                .withTypeText(typeText)
                .withCaseSensitivity(false)

        private fun iconFor(kind: RpgSymbol.Kind): Icon = when (kind) {
            RpgSymbol.Kind.PROCEDURE,
            RpgSymbol.Kind.PROTOTYPE,
            RpgSymbol.Kind.SUBROUTINE -> AllIcons.Nodes.Method
            RpgSymbol.Kind.DATA_STRUCTURE -> AllIcons.Nodes.Class
            RpgSymbol.Kind.FILE,
            RpgSymbol.Kind.CONSTANT -> AllIcons.Nodes.Field
            RpgSymbol.Kind.VARIABLE -> AllIcons.Nodes.Variable
        }
    }
}
