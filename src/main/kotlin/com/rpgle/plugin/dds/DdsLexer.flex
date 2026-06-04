package com.rpgle.plugin.dds;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.rpgle.plugin.dds.DdsTokenTypes;

%%

%public
%class _DdsLexer
%implements FlexLexer
%unicode
%caseless
%function advance
%type IElementType

WS            = [ \t\f]+
NL            = \R
STRING        = "'" ( [^'] | "''" )* "'"

SPECIAL       = "*" [A-Za-z] [A-Za-z0-9]*

NUMBER        = [0-9]+ ( "." [0-9]+ )?
IDENT         = [A-Za-z@#$] [A-Za-z0-9@#$_]*

KEYWORD       = "UNIQUE" | "REF" | "REFFLD" | "REFACCPTH" | "FORMAT" | "TEXT"
              | "COLHDG" | "ALIAS" | "ALWNULL" | "CCSID" | "CHECK" | "CHKMSG"
              | "CMP" | "COMP" | "CONCAT" | "DATFMT" | "TIMFMT" | "DFT"
              | "DIGITS" | "EDTCDE" | "EDTWRD" | "VALUES" | "RANGE" | "VARLEN"
              | "REFSHIFT" | "ABSVAL" | "ASCEND" | "DESCEND" | "NOALTSEQ"
              | "SIGNED" | "UNSIGNED" | "ZONE" | "FIFO" | "LIFO" | "FCFO"
              | "DYNSLT" | "PFILE" | "JFILE" | "JOIN" | "JFLD" | "JDFTVAL"
              | "JREF" | "RENAME" | "SST" | "TRNTBL" | "ALL"

%%

^ [0-9 ]* "A"? "*" [^\r\n]*   { return DdsTokenTypes.COMMENT; }

{WS}            { return TokenType.WHITE_SPACE; }
{NL}            { return TokenType.WHITE_SPACE; }
{STRING}        { return DdsTokenTypes.STRING; }
{KEYWORD}       { return DdsTokenTypes.KEYWORD; }
{SPECIAL}       { return DdsTokenTypes.SPECIAL; }
{NUMBER}        { return DdsTokenTypes.NUMBER; }
{IDENT}         { return DdsTokenTypes.IDENTIFIER; }

"("             { return DdsTokenTypes.LPAREN; }
")"             { return DdsTokenTypes.RPAREN; }

[^]             { return DdsTokenTypes.TEXT; }
