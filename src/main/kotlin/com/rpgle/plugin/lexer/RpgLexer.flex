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

%state IN_SQL

NL            = \R
WHITE_SPACE   = [ \t\f]+
LINE_COMMENT  = "//" [^\r\n]*
SQL_COMMENT   = "--" [^\r\n]*

HYPHENATED    = "DCL-PROC" | "DCL-PR" | "DCL-PI" | "DCL-F" | "DCL-DS" | "DCL-S"
              | "DCL-C" | "DCL-SUBF" | "DCL-PARM" | "DCL-ENUM" | "CTL-OPT"
              | "END-PROC" | "END-PR" | "END-PI" | "END-DS" | "END-ENUM"
              | "EVAL-CORR" | "FOR-EACH" | "ON-ERROR" | "ON-EXIT"
              | "DATA-INTO" | "DATA-GEN" | "XML-INTO" | "XML-SAX" | "SND-MSG"
              | "Z-ADD" | "Z-SUB"

DIRECTIVE     = "/" ( "COPY" | "INCLUDE" | "FREE" | "END-FREE" | "IF" | "ELSEIF"
              | "ELSE" | "ENDIF" | "DEFINE" | "UNDEFINE" | "EOF" | "EJECT"
              | "SPACE" | "TITLE" | "SET" | "RESTORE" )

STRING        = [xXdDtTzZgGuU]? "'" ( [^'] | "''" )* "'"

DIGIT         = [0-9]
NUMBER        = {DIGIT}+ ( "." {DIGIT}+ )?
BIF           = "%" [A-Za-z] [A-Za-z0-9]*
IDENT         = [A-Za-z@#$] [A-Za-z0-9@#$_]*
OPERATOR      = "<=" | ">=" | "<>" | "**" | "+=" | "-=" | "*=" | "/=" | [-+*/=<>]

EXEC_SQL      = "/"? "EXEC" {WHITE_SPACE} "SQL"
END_EXEC      = "/"? "END-EXEC"

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

SQL_CURSOR_KW = "DECLARE" | "CURSOR" | "OPEN" | "FETCH" | "CLOSE"

%%

<YYINITIAL> {
  ^ [0-9 ]* "*" [^A-Za-z0-9\r\n] [^\r\n]*   { return RpgTokenTypes.COMMENT; }
  ^ [0-9 ]* "*" / {NL}                       { return RpgTokenTypes.COMMENT; }
  ^ "**" [^\r\n]*                            { return RpgTokenTypes.COMMENT; }

  {EXEC_SQL}      { yybegin(IN_SQL); return RpgTokenTypes.SQL_KEYWORD; }

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
  "."             { return RpgTokenTypes.DOT; }
  {OPERATOR}      { return RpgTokenTypes.OPERATOR; }

  [^]             { return TokenType.BAD_CHARACTER; }
}

<IN_SQL> {
  ";"             { yybegin(YYINITIAL); return RpgTokenTypes.SEMICOLON; }
  {END_EXEC}      { yybegin(YYINITIAL); return RpgTokenTypes.SQL_KEYWORD; }

  {WHITE_SPACE}   { return TokenType.WHITE_SPACE; }
  {NL}            { return TokenType.WHITE_SPACE; }
  {SQL_COMMENT}   { return RpgTokenTypes.COMMENT; }
  {LINE_COMMENT}  { return RpgTokenTypes.COMMENT; }
  "/*" ~"*/"      { return RpgTokenTypes.COMMENT; }
  {STRING}        { return RpgTokenTypes.STRING; }

  ":" {IDENT}     { return RpgTokenTypes.SQL_HOST_VAR; }
  "?"             { return RpgTokenTypes.SQL_HOST_VAR; }

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
