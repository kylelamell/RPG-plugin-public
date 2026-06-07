package com.rpgle.plugin.common

import com.intellij.lang.Language

/**
 * Base for the plugin's case-insensitive flat languages (RPG, Binder, DDS). IBM i
 * languages are all case-insensitive, so the only shared behavior is fixing
 * [isCaseSensitive] to false. Subclasses are `object`s that still supply their own
 * `readResolve` to preserve the singleton across deserialization.
 */
abstract class FlatLanguage(id: String) : Language(id) {
    final override fun isCaseSensitive(): Boolean = false
}
