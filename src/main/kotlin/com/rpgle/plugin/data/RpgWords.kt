package com.rpgle.plugin.data

/**
 * Static RPG vocabulary, all stored UPPERCASE, shared by the annotator (coloring) and the
 * completion contributor. Embedded SQL words are catalogued separately so they color distinctly.
 */
object RpgWords {

    val BIFS: Set<String> = setOf(
        "%ABS", "%ADDR", "%ALLOC", "%BITAND", "%BITNOT", "%BITOR", "%BITXOR",
        "%CHAR", "%CHECK", "%CHECKR", "%CONCAT", "%CONCATARR", "%DATE", "%DAYS", "%DEC", "%DECH",
        "%DECPOS", "%DIFF", "%DIV", "%EDITC", "%EDITFLT", "%EDITW", "%ELEM",
        "%EOF", "%EQUAL", "%ERROR", "%FIELDS", "%FLOAT", "%FOUND", "%GRAPH",
        "%HANDLER", "%HOURS", "%INT", "%INTH", "%KDS", "%LEFT", "%LEN", "%LIST",
        "%LOOKUP", "%LOOKUPLT", "%LOOKUPLE", "%LOOKUPGT", "%LOOKUPGE", "%LOWER",
        "%MAX", "%MAXARR", "%MIN", "%MINARR", "%MINUTES", "%MONTHS", "%MSECONDS", "%NULLIND", "%OCCUR",
        "%OPEN", "%OMITTED", "%PADDR", "%PARMS", "%PARMNUM", "%PASSED", "%PROC", "%RANGE", "%REALLOC",
        "%REM", "%REPLACE", "%RIGHT", "%SCAN", "%SCANR", "%SCANRPL", "%SECONDS", "%SHTDN",
        "%SIZE", "%SPLIT", "%SQRT", "%STATUS", "%STR", "%SUBARR", "%SUBDT",
        "%SUBST", "%TARGET", "%THIS", "%TIME", "%TIMESTAMP", "%TLOOKUP",
        "%TLOOKUPLT", "%TLOOKUPLE", "%TLOOKUPGT", "%TLOOKUPGE", "%TRIM",
        "%TRIML", "%TRIMR", "%UCS2", "%UNS", "%UNSH", "%UPPER", "%XFOOT",
        "%XLATE", "%YEARS", "%DATA", "%GEN", "%PARSER"
    )

    val OPCODES: Set<String> = setOf(
        "ACQ", "ADD", "ADDDUR", "ALLOC", "BEGSR", "BITOFF", "BITON", "CALL",
        "CALLB", "CALLP", "CAT", "CHAIN", "CHECK", "CHECKR", "CLEAR", "CLOSE",
        "COMMIT", "COMP", "DEALLOC", "DEFINE", "DELETE", "DIV", "DO", "DOU",
        "DOW", "DSPLY", "DUMP", "ELSE", "ELSEIF", "END", "ENDCS", "ENDDO",
        "ENDFOR", "ENDIF", "ENDMON", "ENDSL", "ENDSR", "EVAL", "EVALR",
        "EXCEPT", "EXFMT", "EXSR", "EXTRCT", "FEOD", "FOR", "FORCE", "GOTO",
        "IF", "IN", "ITER", "KFLD", "KLIST", "LEAVE", "LEAVESR", "LOOKUP",
        "MONITOR", "MOVE", "MOVEA", "MOVEL", "MULT", "MVR", "NEXT", "OCCUR",
        "ON-ERROR", "ON-EXIT", "OPEN", "OTHER", "OUT", "PARM", "PLIST", "POST",
        "READ", "READC", "READE", "READP", "READPE", "REL", "RESET", "RETURN",
        "ROLBK", "SCAN", "SELECT", "SETGT", "SETLL", "SETOFF", "SETON", "SHTDN",
        "SORTA", "SQRT", "SUB", "SUBDUR", "SUBST", "TAG", "TEST", "TIME",
        "UNLOCK", "UPDATE", "WHEN", "WRITE", "XFOOT", "XLATE", "Z-ADD", "Z-SUB",
        "DATA-INTO", "DATA-GEN", "XML-INTO", "XML-SAX", "SND-MSG", "EVAL-CORR",
        "FOR-EACH"
    )

    val SQL_KEYWORDS: Set<String> = setOf(
        "SELECT", "ALL", "DISTINCT", "INTO", "FROM", "WHERE", "GROUP", "BY",
        "HAVING", "ORDER", "ASC", "DESC", "INSERT", "UPDATE", "DELETE", "MERGE",
        "SET", "VALUES", "FOR", "OF", "HOLD", "WITH", "WITHOUT", "AS", "ON", "USING",
        "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "OUTER", "CROSS", "UNION",
        "EXCEPT", "INTERSECT",
        "AND", "OR", "NOT", "NULL", "IS", "IN", "EXISTS", "BETWEEN", "LIKE",
        "ESCAPE", "CASE", "WHEN", "THEN", "ELSE", "END", "CAST",
        "COUNT", "SUM", "AVG", "MIN", "MAX", "COALESCE", "NULLIF",
        "CREATE", "ALTER", "DROP", "TABLE", "VIEW", "INDEX", "ALIAS",
        "PROCEDURE", "FUNCTION", "TRIGGER", "PRIMARY", "KEY", "FOREIGN",
        "REFERENCES", "UNIQUE", "CHECK", "CONSTRAINT", "DEFAULT", "GRANT",
        "REVOKE",
        "COMMIT", "ROLLBACK", "SAVEPOINT", "PREPARE", "EXECUTE", "IMMEDIATE",
        "DESCRIBE", "CALL", "CONNECT", "DISCONNECT", "RELEASE", "OPTION",
        "ISOLATION", "ONLY", "ROWS", "ROW", "NEXT", "FIRST", "OFFSET",
        "CURRENT", "OPTIMIZE", "DYNAMIC", "SCROLL", "RESULT", "GET",
        "DIAGNOSTICS"
    )

    val SQL_CURSOR_KEYWORDS: Set<String> = setOf(
        "DECLARE", "CURSOR", "OPEN", "FETCH", "CLOSE"
    )

    val DECL_KEYWORDS: Set<String> = setOf(
        "DCL-PROC", "DCL-PR", "DCL-PI", "DCL-F", "DCL-DS", "DCL-S", "DCL-C",
        "DCL-SUBF", "DCL-PARM", "DCL-ENUM", "CTL-OPT",
        "END-PROC", "END-PR", "END-PI", "END-DS", "END-ENUM"
    )

    val DATA_TYPES: Set<String> = setOf(
        "CHAR", "VARCHAR", "INT", "UNS", "PACKED", "ZONED", "BINDEC", "FLOAT",
        "IND", "DATE", "TIME", "TIMESTAMP", "POINTER", "GRAPH", "VARGRAPH",
        "UCS2", "VARUCS2", "OBJECT", "LIKE", "LIKEDS", "LIKEREC"
    )

    val KEYWORD_ARGS: Set<String> = setOf(
        "EXTPGM", "EXTPROC", "DIM", "CONST", "VALUE", "OPTIONS", "QUALIFIED",
        "TEMPLATE", "INZ", "BASED", "EXPORT", "IMPORT", "STATIC", "DATFMT",
        "TIMFMT", "PROCPTR", "ALIGN", "OVERLAY", "OCCURS", "DTAARA", "PREFIX",
        "EXTNAME", "EXTFLD", "EXTFMT", "ALTSEQ", "ASCEND", "DESCEND",
        "DISK", "PRINTER", "WORKSTN", "SEQ", "SPECIAL", "KEYED", "USAGE",
        "USROPN", "RENAME", "IGNORE", "INCLUDE", "BLOCK", "EXTFILE", "EXTMBR",
        "INFDS", "INFSR", "SFILE", "COMMIT", "MAIN", "NOMAIN", "DFTACTGRP",
        "ACTGRP", "BNDDIR", "OPTION", "DECEDIT", "COPYRIGHT", "POS", "ENUM"
    )

    val COMPLETION_KEYWORDS: List<String> =
        (OPCODES + DECL_KEYWORDS + DATA_TYPES + KEYWORD_ARGS).sorted()
}
