package com.rpgle.plugin.scan

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager

/**
 * Symbol names declared in a single RPG file, derived from [RpgSymbolScanner] and
 * cached per file. Names are stored UPPERCASE because RPG is case-insensitive;
 * callers uppercase the source token before lookup.
 *
 * Two sets, used by the annotator to color procedures at their *call* sites (the
 * lexer/highlighter can only see a declaration):
 *  - [procedureNames]: procedures and prototypes declared in the file. Colored
 *    with the procedure-name attribute wherever they are called.
 *  - [declaredNames]: every symbol declared in the file (procedures, files,
 *    variables, constants, data structures, subroutines). Used to tell a call of
 *    a locally-declared thing from a call of a procedure that is *not* defined
 *    here — the latter is assumed to live in a bound service program and is
 *    colored like an external file.
 *
 * Unlike its predecessor this does **not** resolve `/COPY`/`/INCLUDE` prototypes:
 * a procedure reached through an include isn't "defined in this file", so at a
 * call site it falls into the service-program case rather than being treated as
 * a local procedure.
 */
object RpgLocalSymbols {

    private data class Names(val procedures: Set<String>, val declared: Set<String>)

    /**
     * Stored under an explicit key (rather than the provider's auto-derived one)
     * so [dropCache] can evict it deterministically when the editor is closed.
     */
    private val NAMES_KEY: Key<CachedValue<Names>> = Key.create("rpg.local.symbols")

    /** UPPERCASE procedure / prototype names declared in [file]. */
    fun procedureNames(file: PsiFile): Set<String> = namesFor(file).procedures

    /** UPPERCASE names of every symbol declared in [file]. */
    fun declaredNames(file: PsiFile): Set<String> = namesFor(file).declared

    /** Drops the cached name sets for [file]; the next lookup recomputes them. */
    fun dropCache(file: PsiFile) {
        file.putUserData(NAMES_KEY, null)
    }

    private fun namesFor(file: PsiFile): Names =
        CachedValuesManager.getCachedValue(file, NAMES_KEY) {
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
