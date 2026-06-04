package com.rpgle.plugin.scan

data class RpgSymbol(
    val name: String,
    val kind: Kind,
    val sourceFile: String? = null,
    val typeText: String? = null,
    val value: String? = null,
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
