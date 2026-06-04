package com.rpgle.plugin.scan

import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * UPPERCASE symbol names declared in a single RPG file (from [RpgSymbolScanner], cached per
 * file), used by the annotator to color procedures at their call sites. `/COPY` includes are
 * not resolved, so an included procedure falls into the bound service-program case.
 */
object RpgLocalSymbols {

    private data class Names(val procedures: Set<String>, val declared: Set<String>)

    /** UPPERCASE procedure / prototype names declared in [file]. */
    fun procedureNames(file: PsiFile): Set<String> = namesFor(file).procedures

    /** UPPERCASE names of every symbol declared in [file]. */
    fun declaredNames(file: PsiFile): Set<String> = namesFor(file).declared

    private fun namesFor(file: PsiFile): Names =
        CachedValuesManager.getCachedValue(file) {
            CachedValueProvider.Result.create(compute(file), file)
        }

    private fun compute(file: PsiFile): Names {
        val procedures = HashSet<String>()
        val declared = HashSet<String>()
        for (symbol in RpgSymbolScanner.scan(file).symbols) {
            val upper = symbol.name.uppercase()
            declared.add(upper)
            if (symbol.kind == RpgSymbol.Kind.PROCEDURE || symbol.kind == RpgSymbol.Kind.PROTOTYPE) {
                procedures.add(upper)
            }
        }
        return Names(procedures, declared)
    }
}
