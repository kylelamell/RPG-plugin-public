package com.rpgle.plugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.documentation.RpgDocumentationProvider
import com.rpgle.plugin.scan.RpgSymbol
import com.rpgle.plugin.scan.RpgSymbolScanner

/**
 * Covers the symbol detail recovered for hover documentation and the end-to-end documentation
 * provider. Resolution is confined to the current file.
 */
class RpgDocumentationTest : BasePlatformTestCase() {

    private fun symbol(name: String) =
        RpgSymbolScanner.scan(myFixture.file).symbols.first { it.name.equals(name, ignoreCase = true) }

    fun testVariableType() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-s amount packed(11:2) inz(0);
            dcl-s name char(50);
            """.trimIndent()
        )
        assertEquals("packed(11:2)", symbol("amount").typeText)
        assertEquals("char(50)", symbol("name").typeText)
    }

    fun testConstantValue() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-c MAX_ROWS 100;
            dcl-c GREETING const('Hello');
            """.trimIndent()
        )
        assertEquals("100", symbol("MAX_ROWS").value)
        assertEquals("'Hello'", symbol("GREETING").value)
    }

    fun testProcedureInterface() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-proc getCustomer;
              dcl-pi *n char(50);
                id int(10) const;
                active ind;
              end-pi;
              return 'x';
            end-proc;
            """.trimIndent()
        )
        val proc = symbol("getCustomer")
        assertEquals(RpgSymbol.Kind.PROCEDURE, proc.kind)
        assertEquals("char(50)", proc.typeText)
        val params = proc.parameters!!
        assertEquals(2, params.size)
        assertEquals("id", params[0].name)
        assertEquals("int(10)", params[0].type)
        assertEquals("active", params[1].name)
        assertEquals("ind", params[1].type)
    }

    fun testPrototypeInterface() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-pr lookupName varchar(100);
              key packed(7:0) const;
            end-pr;
            """.trimIndent()
        )
        val proto = symbol("lookupName")
        assertEquals(RpgSymbol.Kind.PROTOTYPE, proto.kind)
        assertEquals("varchar(100)", proto.typeText)
        assertEquals(1, proto.parameters!!.size)
        assertEquals("key", proto.parameters!![0].name)
        assertEquals("packed(7:0)", proto.parameters!![0].type)
    }

    fun testProcedureWithoutReturnHasNoType() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-proc run;
              dcl-pi *n;
                arg char(1);
              end-pi;
            end-proc;
            """.trimIndent()
        )
        val proc = symbol("run")
        assertNull("a procedure with no return type should report none", proc.typeText)
        assertEquals(1, proc.parameters!!.size)
        assertEquals("arg", proc.parameters!![0].name)
    }

    fun testBodylessPrototypeHasNoParams() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-pr getTimestamp timestamp;
            dcl-proc run;
              now = getTimestamp();
            end-proc;
            """.trimIndent()
        )
        val proto = symbol("getTimestamp")
        assertEquals("timestamp", proto.typeText)
        assertEquals("a bodyless prototype takes no parameters", 0, proto.parameters!!.size)
    }

    fun testDocHtmlForVariable() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-s tot<caret>al packed(11:2);
            """.trimIndent()
        )
        val doc = generateDocAtCaret()
        assertNotNull("expected documentation for a variable", doc)
        assertTrue("expected the variable label, got: $doc", doc!!.contains("variable"))
        assertTrue("expected the type, got: $doc", doc.contains("packed(11:2)"))
    }

    /**
     * Documentation is single-file: hovering an identifier declared only in another file (even one
     * reachable via `/COPY`) must resolve to nothing.
     */
    fun testDocumentationDoesNotResolveSymbolFromAnotherFile() {
        myFixture.addFileToProject(
            "other.rpgle",
            """
            **free
            dcl-s amountFromOther packed(11:2);
            """.trimIndent()
        )
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            /copy other
            dcl-proc run;
              amountFrom<caret>Other = 1;
            end-proc;
            """.trimIndent()
        )
        assertNull("documentation must not bleed across files", generateDocAtCaret())
    }

    private fun generateDocAtCaret(): String? {
        val provider = RpgDocumentationProvider()
        val context = myFixture.file.findElementAt(myFixture.caretOffset)
        val target = provider.getCustomDocumentationElement(
            myFixture.editor, myFixture.file, context, myFixture.caretOffset,
        )
        return provider.generateDoc(target, context)
    }
}
