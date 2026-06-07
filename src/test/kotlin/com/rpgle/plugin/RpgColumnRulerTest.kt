package com.rpgle.plugin

import com.rpgle.plugin.ruler.RpgColumnRuler
import org.junit.Assert.assertEquals
import org.junit.Test

class RpgColumnRulerTest {

    @Test
    fun rulerFollowsTheTensFivesAndDotsSpec() {
        val ruler = RpgColumnRuler.buildRuler(100)

        assertEquals("ruler must stop at column 100", 100, ruler.length)

        // Tenth columns show the leading digit of the column number.
        assertEquals('1', ruler[9])    // column 10
        assertEquals('2', ruler[19])   // column 20
        assertEquals('5', ruler[49])   // column 50
        assertEquals('9', ruler[89])   // column 90
        assertEquals('0', ruler[99])   // column 100 -> reset to 0 in case I want to expand past 100

        // Remaining fifth columns are 'x'.
        assertEquals('x', ruler[4])    // column 5
        assertEquals('x', ruler[14])   // column 15
        assertEquals('x', ruler[94])   // column 95

        // Everything else is a dot.
        assertEquals('.', ruler[0])    // column 1
        assertEquals('.', ruler[7])    // column 8

        val expected =
            "....x....1....x....2....x....3....x....4....x....5" +
                    "....x....6....x....7....x....8....x....9....x....0"
        assertEquals(expected, ruler)
    }
}
