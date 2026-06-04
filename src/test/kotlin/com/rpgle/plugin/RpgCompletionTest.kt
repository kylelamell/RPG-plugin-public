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

    fun testCopyPrototypeCompletion() {
        myFixture.addFileToProject(
            "protos.rpgleinc",
            """
            dcl-pr getCustomer char(50);
              id int(10) const;
            end-pr;
            dcl-pr getCustomerName char(50);
              id int(10) const;
            end-pr;
            """.trimIndent()
        )
        myFixture.configureByText(
            "main.rpgle",
            """
            **free
            /copy protos
            dcl-proc run;
              dsply getCust<caret>;
            end-proc;
            """.trimIndent()
        )
        myFixture.completeBasic()
        val items = myFixture.lookupElementStrings
        assertNotNull(items)
        assertTrue("expected getCustomer from /COPY, got $items", items!!.contains("getCustomer"))
    }
}
