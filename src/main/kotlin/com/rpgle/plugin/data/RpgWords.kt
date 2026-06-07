package com.rpgle.plugin.data

/**
 * Static RPG vocabulary, all stored UPPERCASE. RPG is case-insensitive, so
 * callers uppercase the source token before lookup. Shared by the annotator
 * (coloring) and the completion contributor (suggestions). Embedded SQL words
 * are catalogued separately in [SQL_KEYWORDS] — with cursor-lifecycle words in
 * [SQL_CURSOR_KEYWORDS] — so they color distinctly from RPG opcodes.
 */
object RpgWords {

    /** Built-in functions, stored WITH the leading '%'. */
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

    /** Operation codes (free and fixed format). */
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

    /**
     * SQL words colored inside an EXEC SQL block. Kept as its own category
     * (distinct from [OPCODES]) so embedded SQL stands out with its own color —
     * note several words such as SELECT/OPEN/CLOSE/COMMIT/FOR/IN/WHEN also exist
     * as RPG opcodes, but inside an EXEC SQL block they are SQL, not opcodes.
     *
     * The lexer's IN_SQL state (RpgLexer.flex) is what actually classifies these
     * at lex time; this set mirrors that vocabulary in one readable place.
     * `RpgWordsSqlSyncTest` asserts the two stay identical so the mirror can't
     * silently drift.
     */
    val SQL_KEYWORDS: Set<String> = setOf(
        // Statement keywords / clauses
        "SELECT", "ALL", "DISTINCT", "INTO", "FROM", "WHERE", "GROUP", "BY",
        "HAVING", "ORDER", "ASC", "DESC", "INSERT", "UPDATE", "DELETE", "MERGE",
        "SET", "VALUES", "FOR", "OF", "HOLD", "WITH", "WITHOUT", "AS", "ON", "USING",
        // Joins / set operators
        "JOIN", "INNER", "LEFT", "RIGHT", "FULL", "OUTER", "CROSS", "UNION",
        "EXCEPT", "INTERSECT",
        // Predicates / logical
        "AND", "OR", "NOT", "NULL", "IS", "IN", "EXISTS", "BETWEEN", "LIKE",
        "ESCAPE", "CASE", "WHEN", "THEN", "ELSE", "END", "CAST",
        // Aggregates / common functions
        "COUNT", "SUM", "AVG", "MIN", "MAX", "COALESCE", "NULLIF",
        // DDL
        "CREATE", "ALTER", "DROP", "TABLE", "VIEW", "INDEX", "ALIAS",
        "PROCEDURE", "FUNCTION", "TRIGGER", "PRIMARY", "KEY", "FOREIGN",
        "REFERENCES", "UNIQUE", "CHECK", "CONSTRAINT", "DEFAULT", "GRANT",
        "REVOKE",
        // Transaction / dynamic SQL / cursor controls
        "COMMIT", "ROLLBACK", "SAVEPOINT", "PREPARE", "EXECUTE", "IMMEDIATE",
        "DESCRIBE", "CALL", "CONNECT", "DISCONNECT", "RELEASE", "OPTION",
        "ISOLATION", "ONLY", "ROWS", "ROW", "NEXT", "FIRST", "OFFSET",
        "CURRENT", "OPTIMIZE", "DYNAMIC", "SCROLL", "RESULT", "GET",
        "DIAGNOSTICS"
    )

    /**
     * Cursor definition / lifecycle keywords. Split out of [SQL_KEYWORDS] so the
     * lexer can color them distinctly (a slightly darker blue) within an EXEC SQL
     * block. Mirrors the SQL_CURSOR_KW macro in RpgLexer.flex — keep both in sync
     * (enforced by `RpgWordsSqlSyncTest`).
     */
    val SQL_CURSOR_KEYWORDS: Set<String> = setOf(
        "DECLARE", "CURSOR", "OPEN", "FETCH", "CLOSE"
    )

    /** Declaration / structure keywords (free format). */
    val DECL_KEYWORDS: Set<String> = setOf(
        "DCL-PROC", "DCL-PR", "DCL-PI", "DCL-F", "DCL-DS", "DCL-S", "DCL-C",
        "DCL-SUBF", "DCL-PARM", "DCL-ENUM", "CTL-OPT",
        "END-PROC", "END-PR", "END-PI", "END-DS", "END-ENUM"
    )

    /** Data types. */
    val DATA_TYPES: Set<String> = setOf(
        "CHAR", "VARCHAR", "INT", "UNS", "PACKED", "ZONED", "BINDEC", "FLOAT",
        "IND", "DATE", "TIME", "TIMESTAMP", "POINTER", "GRAPH", "VARGRAPH",
        "UCS2", "VARUCS2", "OBJECT", "LIKE", "LIKEDS", "LIKEREC"
    )

    /** Definition keyword arguments (D-spec / F-spec / control keywords). */
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

    /** Everything offered as a plain keyword completion. */
    val COMPLETION_KEYWORDS: List<String> =
        (OPCODES + DECL_KEYWORDS + DATA_TYPES + KEYWORD_ARGS).sorted()
}
