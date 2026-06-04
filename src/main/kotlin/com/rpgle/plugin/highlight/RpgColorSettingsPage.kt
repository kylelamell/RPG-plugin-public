package com.rpgle.plugin.highlight

import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.ColorSettingsPage
import com.rpgle.plugin.RpgIcons
import javax.swing.Icon

class RpgColorSettingsPage : ColorSettingsPage {

    override fun getIcon(): Icon = RpgIcons.FILE

    override fun getHighlighter(): SyntaxHighlighter = RpgSyntaxHighlighter()

    override fun getDemoText(): String = """
        **free
        ctl-opt dftactgrp(*no) main(<proc>Main</proc>);

        /copy qrpgleref,custproc

        dcl-f <file>CUSTMAST</file> disk keyed;

        // Calculate an order total
        dcl-proc <proc>Main</proc>;
          dcl-pi *n;
          end-pi;
          dcl-s total packed(11:2);
          total = <op>%dec</op>(price) * qty;
          // calcDiscount is declared nowhere here, so it's assumed to live in a
          // bound service program and colored like an external file
          total = total - <srv>calcDiscount</srv>(total);
          if total > 1000;
            dsply ('Big order: ' + %char(total));
          endif;
          exsr <sr>LogOrder</sr>;

          exec sql
            select name, balance into :custName, :balance
            from custmast where custno = :id and active = 'Y';

          exec sql declare c1 cursor for
            select name from custmast where active = 'Y';
          exec sql open c1;
          exec sql fetch c1 into :custName;
          exec sql close c1;

          begsr <sr>LogOrder</sr>;
            dsply ('Order logged: ' + %char(total));
          endsr;
        end-proc;
    """.trimIndent()

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> =
        mapOf(
            "proc" to RpgSyntaxHighlighter.PROC_NAME,
            "sr" to RpgSyntaxHighlighter.SUBROUTINE_NAME,
            "file" to RpgSyntaxHighlighter.FILE_NAME,
            "srv" to RpgSyntaxHighlighter.SERVICE_PROC,
            "op" to RpgSyntaxHighlighter.BIF,
            "kw" to RpgSyntaxHighlighter.KEYWORD,
            "dt" to RpgSyntaxHighlighter.DATA_TYPE,
        )

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> = DESCRIPTORS

    override fun getColorDescriptors(): Array<ColorDescriptor> = ColorDescriptor.EMPTY_ARRAY

    override fun getDisplayName(): String = "RPG"

    companion object {
        private val DESCRIPTORS = arrayOf(
            AttributesDescriptor("Comment", RpgSyntaxHighlighter.LINE_COMMENT),
            AttributesDescriptor("String", RpgSyntaxHighlighter.STRING),
            AttributesDescriptor("Number", RpgSyntaxHighlighter.NUMBER),
            AttributesDescriptor("Built-in function (BIF)", RpgSyntaxHighlighter.BIF),
            AttributesDescriptor("Compiler directive", RpgSyntaxHighlighter.DIRECTIVE),
            AttributesDescriptor("Operator", RpgSyntaxHighlighter.OPERATOR),
            AttributesDescriptor("Parentheses", RpgSyntaxHighlighter.PARENTHESES),
            AttributesDescriptor("Separator", RpgSyntaxHighlighter.SEPARATOR),
            AttributesDescriptor("Embedded SQL//Keyword", RpgSyntaxHighlighter.SQL_KEYWORD),
            AttributesDescriptor("Embedded SQL//Cursor keyword", RpgSyntaxHighlighter.SQL_CURSOR_KEYWORD),
            AttributesDescriptor("Embedded SQL//Host variable", RpgSyntaxHighlighter.SQL_HOST_VAR),
            AttributesDescriptor("Keyword", RpgSyntaxHighlighter.KEYWORD),
            AttributesDescriptor("Operation code", RpgSyntaxHighlighter.OPCODE),
            AttributesDescriptor("Data type", RpgSyntaxHighlighter.DATA_TYPE),
            AttributesDescriptor("Procedure name", RpgSyntaxHighlighter.PROC_NAME),
            AttributesDescriptor("Subroutine name", RpgSyntaxHighlighter.SUBROUTINE_NAME),
            AttributesDescriptor("File name", RpgSyntaxHighlighter.FILE_NAME),
            AttributesDescriptor("Service-program procedure", RpgSyntaxHighlighter.SERVICE_PROC),
            AttributesDescriptor("Bad character", RpgSyntaxHighlighter.BAD_CHARACTER),
        )
    }
}
