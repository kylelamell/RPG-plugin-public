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

// *ISO, *DATE, *NULL, *BLANK, *ZERO, *USER, *JOB, *CAT, *ALL, ... (mid-line).
SPECIAL       = "*" [A-Za-z] [A-Za-z0-9]*

NUMBER        = [0-9]+ ( "." [0-9]+ )?
IDENT         = [A-Za-z@#$] [A-Za-z0-9@#$_]*

// DDS keywords for physical (.pf) and logical (.lf) files: file/record level,
// field level, key level, and the join/select keywords used by logical files.
KEYWORD       = "UNIQUE" | "REF" | "REFFLD" | "REFACCPTH" | "FORMAT" | "TEXT"
              | "COLHDG" | "ALIAS" | "ALWNULL" | "CCSID" | "CHECK" | "CHKMSG"
              | "CMP" | "COMP" | "CONCAT" | "DATFMT" | "TIMFMT" | "DFT"
              | "DIGITS" | "EDTCDE" | "EDTWRD" | "VALUES" | "RANGE" | "VARLEN"
              | "REFSHIFT" | "ABSVAL" | "ASCEND" | "DESCEND" | "NOALTSEQ"
              | "SIGNED" | "UNSIGNED" | "ZONE" | "FIFO" | "LIFO" | "FCFO"
              | "DYNSLT" | "PFILE" | "JFILE" | "JOIN" | "JFLD" | "JDFTVAL"
              | "JREF" | "RENAME" | "SST" | "TRNTBL" | "ALL"

%%

// DDS comment: an asterisk in position 7. Allow optional sequence-number columns
// (digits/blanks, positions 1-5) and the optional 'A' form type in position 6,
// so this matches "     A* note", "A* note" and "      * note" regardless of
// whether the source carries sequence numbers. A field/record line such as
// "     A          R FMT" is left alone because the '*' must come immediately
// after the leading columns and optional single 'A'. ('^' is only valid here in
// a rule, not in a macro.)
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

// Everything else (punctuation such as / . : + -, stray characters) is carried
// as neutral text so nothing in a best-effort DDS file is flagged as an error.
[^]             { return DdsTokenTypes.TEXT; }
