package com.rpgle.plugin.lexer;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.rpgle.plugin.psi.RpgTokenTypes;

%%

%public
%class _RpgLexer
%implements FlexLexer
%unicode
%caseless
%function advance
%type IElementType

// IN_SQL is entered on "EXEC SQL" and left on ';' or "/END-EXEC". While in it,
// SQL keywords (SELECT/FROM/WHERE/...) and host variables (:var) are coloured.
%state IN_SQL

// SQL_INTRO bridges the two words of the "EXEC SQL" introducer. We enter it on
// EXEC *alone* (no lookahead) and confirm SQL from inside it. This is required
// for the editor's incremental re-lexing: that re-lexer hands the lexer a bounded
// read window, so any rule that has to look *ahead* past the current token to
// classify it (the old "EXEC <ws> SQL" single match, or an "EXEC / <ws> SQL"
// trailing-context match) fails on a freshly typed block — EXEC is read as a
// plain identifier, IN_SQL is never entered, and the new SQL stays un-highlighted
// until the file is reparsed by closing and reopening it. Deciding EXEC purely
// from its own text (then SQL purely from its own text in this state) keeps every
// token locally classifiable. The only cost is that a stray top-level "exec" not
// followed by SQL is briefly coloured as a keyword — a non-issue, since EXEC is
// not an RPG opcode and appears only in EXEC SQL. See docs/highlighting.md.
%state SQL_INTRO

// ---------------------------------------------------------------------------
// Macros
// ---------------------------------------------------------------------------
NL            = \R
WHITE_SPACE   = [ \t\f]+
LINE_COMMENT  = "//" [^\r\n]*
SQL_COMMENT   = "--" [^\r\n]*

// Some keywords/opcodes contain hyphens (modern free-format ones like DCL-PROC,
// plus legacy fixed-format opcodes like Z-ADD); match them as single tokens so
// they are not split into IDENTIFIER '-' IDENTIFIER (the '-' is also minus).
HYPHENATED    = "DCL-PROC" | "DCL-PR" | "DCL-PI" | "DCL-F" | "DCL-DS" | "DCL-S"
              | "DCL-C" | "DCL-SUBF" | "DCL-PARM" | "DCL-ENUM" | "CTL-OPT"
              | "END-PROC" | "END-PR" | "END-PI" | "END-DS" | "END-ENUM"
              | "EVAL-CORR" | "FOR-EACH" | "ON-ERROR" | "ON-EXIT"
              | "DATA-INTO" | "DATA-GEN" | "XML-INTO" | "XML-SAX" | "SND-MSG"
              | "Z-ADD" | "Z-SUB"

DIRECTIVE     = "/" ( "COPY" | "INCLUDE" | "FREE" | "END-FREE" | "IF" | "ELSEIF"
              | "ELSE" | "ENDIF" | "DEFINE" | "UNDEFINE" | "EOF" | "EJECT"
              | "SPACE" | "TITLE" | "SET" | "RESTORE" )

// Optional typed-literal prefix: x'..' (hex) d'..' t'..' z'..' g'..' u'..'
STRING        = [xXdDtTzZgGuU]? "'" ( [^'] | "''" )* "'"

DIGIT         = [0-9]
NUMBER        = {DIGIT}+ ( "." {DIGIT}+ )?
BIF           = "%" [A-Za-z] [A-Za-z0-9]*
IDENT         = [A-Za-z@#$] [A-Za-z0-9@#$_]*
OPERATOR      = "<=" | ">=" | "<>" | "**" | "+=" | "-=" | "*=" | "/=" | [-+*/=<>]

// EXEC introducer (optional leading slash for fixed-format /EXEC SQL). The SQL
// word that follows is matched separately, from the SQL_INTRO state. Plus the
// fixed-format /END-EXEC terminator.
EXEC_INTRO    = "/"? "EXEC"
END_EXEC      = "/"? "END-EXEC"

// SQL words coloured inside an EXEC SQL block. Statement keywords, clauses,
// predicates, joins, set operators, common DDL and aggregate functions.
SQL_KEYWORD   = "SELECT" | "ALL" | "DISTINCT" | "INTO" | "FROM" | "WHERE"
              | "GROUP" | "BY" | "HAVING" | "ORDER" | "ASC" | "DESC"
              | "INSERT" | "UPDATE" | "DELETE" | "MERGE" | "SET" | "VALUES"
              | "FOR" | "OF" | "HOLD" | "WITH" | "WITHOUT" | "AS" | "ON" | "USING"
              | "JOIN" | "INNER" | "LEFT" | "RIGHT" | "FULL" | "OUTER"
              | "CROSS" | "UNION" | "EXCEPT" | "INTERSECT" | "AND" | "OR"
              | "NOT" | "NULL" | "IS" | "IN" | "EXISTS" | "BETWEEN" | "LIKE"
              | "ESCAPE" | "CASE" | "WHEN" | "THEN" | "ELSE" | "END" | "CAST"
              | "COUNT" | "SUM" | "AVG" | "MIN" | "MAX" | "COALESCE" | "NULLIF"
              | "CREATE" | "ALTER" | "DROP" | "TABLE" | "VIEW" | "INDEX"
              | "ALIAS" | "PROCEDURE" | "FUNCTION" | "TRIGGER" | "PRIMARY"
              | "KEY" | "FOREIGN" | "REFERENCES" | "UNIQUE" | "CHECK"
              | "CONSTRAINT" | "DEFAULT" | "GRANT" | "REVOKE" | "COMMIT"
              | "ROLLBACK" | "SAVEPOINT" | "PREPARE" | "EXECUTE" | "IMMEDIATE"
              | "DESCRIBE" | "CALL" | "CONNECT" | "DISCONNECT" | "RELEASE"
              | "OPTION" | "ISOLATION" | "ONLY" | "ROWS" | "ROW" | "NEXT"
              | "FIRST" | "OFFSET" | "CURRENT" | "OPTIMIZE" | "DYNAMIC"
              | "SCROLL" | "RESULT" | "GET" | "DIAGNOSTICS"

// Cursor definition / lifecycle keywords. Colored distinctly from the general
// SQL keywords (a slightly darker blue) so cursor work stands out within an
// EXEC SQL block. Kept narrow to words that are unambiguously about cursors —
// FOR/WITH/HOLD/SCROLL stay general SQL keywords because they appear in many
// non-cursor clauses too and the lexer can't tell those uses apart.
SQL_CURSOR_KW = "DECLARE" | "CURSOR" | "OPEN" | "FETCH" | "CLOSE"

%%

<YYINITIAL> {
  // Fixed-format comment heuristic: optional sequence-number columns (digits or
  // blanks) then '*' that is NOT followed by an alphanumeric. This colours
  //   "      * remark"  and  "     *-----"  while leaving figurative constants
  // such as *INLR / *ON / *BLANKS as ordinary tokens. Best effort only.
  ^ [0-9 ]* "*" [^A-Za-z0-9\r\n] [^\r\n]*   { return RpgTokenTypes.COMMENT; }
  ^ [0-9 ]* "*" / {NL}                       { return RpgTokenTypes.COMMENT; }
  // Compile-time data / **FREE marker at the start of a line.
  ^ "**" [^\r\n]*                            { return RpgTokenTypes.COMMENT; }

  // Enter the EXEC SQL introducer: colour EXEC as an SQL keyword and move to
  // SQL_INTRO, where the following SQL word flips us into IN_SQL. EXEC is decided
  // with no lookahead so incremental re-lexing works (see the SQL_INTRO note).
  {EXEC_INTRO}    { yybegin(SQL_INTRO); return RpgTokenTypes.SQL_KEYWORD; }

  {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }
  {NL}            { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT}  { return RpgTokenTypes.COMMENT; }
  {DIRECTIVE}     { return RpgTokenTypes.DIRECTIVE; }
  {STRING}        { return RpgTokenTypes.STRING; }
  {HYPHENATED}    { return RpgTokenTypes.IDENTIFIER; }
  {BIF}           { return RpgTokenTypes.BIF; }
  {NUMBER}        { return RpgTokenTypes.NUMBER; }
  {IDENT}         { return RpgTokenTypes.IDENTIFIER; }

  ";"             { return RpgTokenTypes.SEMICOLON; }
  "("             { return RpgTokenTypes.LPAREN; }
  ")"             { return RpgTokenTypes.RPAREN; }
  ","             { return RpgTokenTypes.COMMA; }
  ":"             { return RpgTokenTypes.COLON; }
  // Qualified-name separator, e.g. ds.subfield. Tokenized as DOT (not a bad
  // character) so it is left uncolored rather than flagged as an error. A '.'
  // inside a numeric literal is consumed by {NUMBER} above via longest-match.
  "."             { return RpgTokenTypes.DOT; }
  {OPERATOR}      { return RpgTokenTypes.OPERATOR; }

  [^]             { return TokenType.BAD_CHARACTER; }
}

<SQL_INTRO> {
  // Just consumed EXEC. Skip the gap, then SQL drops us into the embedded-SQL
  // body. Anything else means this wasn't "EXEC SQL" after all: push the
  // character back and re-lex it in YYINITIAL (so e.g. a newline, ';', or another
  // word is tokenized normally). The catch-all in YYINITIAL guarantees the
  // pushed-back character is always consumed, so this can't loop.
  {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }
  "SQL"           { yybegin(IN_SQL); return RpgTokenTypes.SQL_KEYWORD; }
  [^]             { yypushback(1); yybegin(YYINITIAL); }
}

<IN_SQL> {
  // Leaving the block: a free-format ';' or a fixed-format /END-EXEC. A ';'
  // inside a quoted literal is consumed by {STRING} below, so it can't end the
  // statement prematurely.
  ";"             { yybegin(YYINITIAL); return RpgTokenTypes.SEMICOLON; }
  {END_EXEC}      { yybegin(YYINITIAL); return RpgTokenTypes.SQL_KEYWORD; }

  {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }
  {NL}            { return TokenType.WHITE_SPACE; }
  {SQL_COMMENT}   { return RpgTokenTypes.COMMENT; }
  {LINE_COMMENT}  { return RpgTokenTypes.COMMENT; }
  "/*" ~"*/"      { return RpgTokenTypes.COMMENT; }
  {STRING}        { return RpgTokenTypes.STRING; }

  // Host variables / parameter markers: :name, :ds.subf, or a bare ? marker.
  ":" {IDENT}     { return RpgTokenTypes.SQL_HOST_VAR; }
  "?"             { return RpgTokenTypes.SQL_HOST_VAR; }

  // Keywords must precede {IDENT}; an exact match wins the rule-order tie while
  // a longer identifier (e.g. FROMDATE) still wins by longest-match. Cursor
  // keywords precede {SQL_KEYWORD} for the same rule-order reason (the two sets
  // are disjoint, so this is just defensive ordering).
  {SQL_CURSOR_KW} { return RpgTokenTypes.SQL_CURSOR_KEYWORD; }
  {SQL_KEYWORD}   { return RpgTokenTypes.SQL_KEYWORD; }
  {NUMBER}        { return RpgTokenTypes.NUMBER; }
  {IDENT}         { return RpgTokenTypes.IDENTIFIER; }

  "("             { return RpgTokenTypes.LPAREN; }
  ")"             { return RpgTokenTypes.RPAREN; }
  ","             { return RpgTokenTypes.COMMA; }
  ":"             { return RpgTokenTypes.COLON; }
  "."             { return RpgTokenTypes.DOT; }
  "||"            { return RpgTokenTypes.OPERATOR; }
  {OPERATOR}      { return RpgTokenTypes.OPERATOR; }

  [^]             { return TokenType.BAD_CHARACTER; }
}
