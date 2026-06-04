package com.rpgle.plugin.binder;

import com.intellij.lexer.FlexLexer;
import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;
import com.rpgle.plugin.binder.BinderTokenTypes;

%%

%public
%class _BinderLexer
%implements FlexLexer
%unicode
%caseless
%function advance
%type IElementType

WS            = [ \t\f]+
NL            = \R
LINE_COMMENT  = "//" [^\r\n]*
BLOCK_COMMENT = "/*" ~"*/"
STRING        = "'" ( [^'] | "''" )* "'"
KEYWORD       = "STRPGMEXP" | "ENDPGMEXP" | "EXPORT" | "PGMLVL" | "LVLCHK" | "SIGNATURE" | "SYMBOL"
SPECIAL       = "*" [A-Za-z0-9]+
NUMBER        = [0-9]+
IDENT         = [A-Za-z] [A-Za-z0-9_]*

%%

<YYINITIAL> {
  {WS}            { return TokenType.WHITE_SPACE; }
  {NL}            { return TokenType.WHITE_SPACE; }
  {LINE_COMMENT}  { return BinderTokenTypes.COMMENT; }
  {BLOCK_COMMENT} { return BinderTokenTypes.COMMENT; }
  {STRING}        { return BinderTokenTypes.STRING; }
  {KEYWORD}       { return BinderTokenTypes.KEYWORD; }
  {SPECIAL}       { return BinderTokenTypes.SPECIAL; }
  {NUMBER}        { return BinderTokenTypes.NUMBER; }
  {IDENT}         { return BinderTokenTypes.IDENTIFIER; }

  "("             { return BinderTokenTypes.LPAREN; }
  ")"             { return BinderTokenTypes.RPAREN; }
  ":"             { return BinderTokenTypes.OPERATOR; }

  [^]             { return TokenType.BAD_CHARACTER; }
}
