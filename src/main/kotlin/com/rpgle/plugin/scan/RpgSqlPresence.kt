package com.rpgle.plugin.scan

import com.intellij.openapi.util.Key
import com.intellij.psi.PsiFile
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.CachedValuesManager
import com.rpgle.plugin.psi.RpgTokenTypes

/**
 * Whether an RPG file contains any embedded SQL, cached per file. The annotator
 * uses this to skip the (otherwise per-identifier) backward scan that decides
 * whether a token sits inside an `EXEC SQL` block — a file with no SQL never pays
 * for that scan. Cached per file and dropped on editor close, like the symbol
 * scan in [RpgSymbolScanner].
 */
object RpgSqlPresence {

    private val KEY: Key<CachedValue<Boolean>> = Key.create("rpg.has.embedded.sql")

    /** True if [file] contains at least one embedded-SQL token. */
    fun hasEmbeddedSql(file: PsiFile): Boolean =
        CachedValuesManager.getCachedValue(file, KEY) {
            CachedValueProvider.Result.create(compute(file), file)
        }

    /** Drops the cached flag for [file]; the next query recomputes it lazily. */
    fun dropCache(file: PsiFile) {
        file.putUserData(KEY, null)
    }

    private fun compute(file: PsiFile): Boolean =
        file.node.getChildren(null).any {
            when (it.elementType) {
                RpgTokenTypes.SQL_KEYWORD,
                RpgTokenTypes.SQL_CURSOR_KEYWORD,
                RpgTokenTypes.SQL_HOST_VAR -> true
                else -> false
            }
        }
}
