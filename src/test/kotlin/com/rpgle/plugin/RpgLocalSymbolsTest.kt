package com.rpgle.plugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.rpgle.plugin.scan.RpgLocalSymbols

/**
 * Backs the annotator's use-site coloring: procedures declared in the file are colored where
 * called, while a call to a name declared nowhere is treated as a bound service-program procedure.
 */
class RpgLocalSymbolsTest : BasePlatformTestCase() {

    fun testLocalProceduresAreCollected() {
        val file = myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-proc calcTotal;
              return 1;
            end-proc;
            dcl-proc run;
              dsply %char(calcTotal());
            end-proc;
            """.trimIndent()
        )
        val procs = RpgLocalSymbols.procedureNames(file)
        assertTrue("expected CALCTOTAL, got $procs", "CALCTOTAL" in procs)
        assertTrue("expected RUN, got $procs", "RUN" in procs)
    }

    fun testLocalPrototypeIsAProcedure() {
        val file = myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-pr getCustomer char(50);
              id int(10) const;
            end-pr;
            """.trimIndent()
        )
        assertTrue("expected GETCUSTOMER", "GETCUSTOMER" in RpgLocalSymbols.procedureNames(file))
    }

    fun testVariableIsDeclaredButNotAProcedure() {
        val file = myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-s total packed(11:2);
            dcl-proc run;
              total = 1;
            end-proc;
            """.trimIndent()
        )
        val procs = RpgLocalSymbols.procedureNames(file)
        val declared = RpgLocalSymbols.declaredNames(file)
        assertFalse("TOTAL is a variable, not a procedure; got $procs", "TOTAL" in procs)
        assertTrue("TOTAL should still be a declared symbol; got $declared", "TOTAL" in declared)
        assertTrue("expected RUN, got $procs", "RUN" in procs)
    }

    fun testIncludedPrototypeIsNotLocal() {
        myFixture.addFileToProject(
            "protos.rpgleinc",
            """
            dcl-pr getCustomer char(50);
              id int(10) const;
            end-pr;
            """.trimIndent()
        )
        val file = myFixture.configureByText(
            "main.rpgle",
            """
            **free
            /copy protos
            dcl-proc run;
              dsply getCustomer(1);
            end-proc;
            """.trimIndent()
        )
        val procs = RpgLocalSymbols.procedureNames(file)
        val declared = RpgLocalSymbols.declaredNames(file)
        assertFalse("GETCUSTOMER comes from /COPY, not this file; got $procs", "GETCUSTOMER" in procs)
        assertFalse("GETCUSTOMER is not declared in this file; got $declared", "GETCUSTOMER" in declared)
        assertTrue("expected RUN, got $procs", "RUN" in procs)
    }
}
