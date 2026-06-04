package com.rpgle.plugin

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class RpgCompletionTest : BasePlatformTestCase() {

    fun testBifCompletion() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-s s char(10);
            s = %tri<caret>;
            """.trimIndent()
        )
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings
        assertNotNull(items)
        assertTrue("expected %TRIM, got $items", items!!.contains("%TRIM"))
    }

    fun testLocalProcedureCompletion() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-proc calcTotal;
              return 1;
            end-proc;
            dcl-proc calcAverage;
              return 2;
            end-proc;
            dcl-proc run;
              dsply %char(calc<caret>);
            end-proc;
            """.trimIndent()
        )
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings
        assertNotNull(items)
        assertTrue("expected calcTotal, got $items", items!!.contains("calcTotal"))
        assertTrue("expected calcAverage, got $items", items.contains("calcAverage"))
    }

    fun testDeclaredFileCompletion() {
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            dcl-f CUSTMAST disk;
            dcl-f CUSTHIST disk;
            dcl-proc run;
              chain key CUST<caret>;
            end-proc;
            """.trimIndent()
        )
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings
        assertNotNull(items)
        assertTrue("expected CUSTMAST, got $items", items!!.contains("CUSTMAST"))
        assertTrue("expected CUSTHIST, got $items", items.contains("CUSTHIST"))
    }

    /**
     * Completion is single-file: a procedure declared only in another file (even one pulled in via
     * `/COPY`) must never be offered, while local procedures sharing the lookup prefix are.
     */
    fun testCompletionDoesNotLeakSymbolsFromAnotherFile() {
        myFixture.addFileToProject(
            "other.rpgle",
            """
            **free
            dcl-proc sharedProcOther;
              return 1;
            end-proc;
            """.trimIndent()
        )
        myFixture.configureByText(
            "a.rpgle",
            """
            **free
            /copy other
            dcl-proc sharedProcLocalA;
              return 1;
            end-proc;
            dcl-proc sharedProcLocalB;
              return 2;
            end-proc;
            dcl-proc run;
              dsply %char(shared<caret>);
            end-proc;
            """.trimIndent()
        )
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings
        assertNotNull(items)
        assertTrue("expected local sharedProcLocalA, got $items", items!!.contains("sharedProcLocalA"))
        assertTrue("expected local sharedProcLocalB, got $items", items.contains("sharedProcLocalB"))
        assertFalse(
            "sharedProcOther is declared in other.rpgle, not here; got $items",
            items.contains("sharedProcOther"),
        )
    }
}
