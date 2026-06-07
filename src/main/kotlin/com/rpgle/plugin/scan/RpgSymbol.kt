package com.rpgle.plugin.scan

data class RpgSymbol(
    val name: String,
    val kind: Kind,
    val sourceFile: String? = null,
    /**
     * Declared type for a [Kind.VARIABLE] (e.g. `"char(10)"`, `"packed(11:2)"`)
     * and the *return* type for a [Kind.PROCEDURE] / [Kind.PROTOTYPE] (null means
     * the procedure returns nothing). Unused for other kinds.
     */
    val typeText: String? = null,
    /** The value of a [Kind.CONSTANT] (e.g. `"100"`, `"'Hello'"`). Unused otherwise. */
    val value: String? = null,
    /**
     * Parameters of a [Kind.PROCEDURE] / [Kind.PROTOTYPE]. Null for the other
     * kinds (and for a procedure with no declared interface); empty for a
     * procedure that is known to take no parameters.
     */
    val parameters: List<RpgParameter>? = null,
) {
    enum class Kind(val label: String) {
        PROCEDURE("procedure"),
        PROTOTYPE("prototype"),
        SUBROUTINE("subroutine"),
        FILE("file"),
        VARIABLE("variable"),
        CONSTANT("constant"),
        DATA_STRUCTURE("data structure"),
    }
}

/** A single procedure / prototype parameter: its name and declared type. */
data class RpgParameter(val name: String, val type: String?)

/** Result of scanning one RPG file: the symbols it declares. */
data class RpgScanResult(
    val symbols: List<RpgSymbol>,
)
