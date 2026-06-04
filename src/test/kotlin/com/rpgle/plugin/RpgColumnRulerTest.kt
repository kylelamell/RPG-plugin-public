package com.rpgle.plugin

import com.rpgle.plugin.ruler.RpgColumnRuler
import org.junit.Assert.assertEquals
import org.junit.Test

class RpgColumnRulerTest {

    @Test
    fun rulerFollowsTheTensFivesAndDotsSpec() {
        val ruler = RpgColumnRuler.buildRuler(100)

        assertEquals("ruler must stop at column 100", 100, ruler.length)

        assertEquals('1', ruler[9])
        assertEquals('2', ruler[19])
        assertEquals('5', ruler[49])
        assertEquals('9', ruler[89])
        assertEquals('0', ruler[99])

        assertEquals('x', ruler[4])
        assertEquals('x', ruler[14])
        assertEquals('x', ruler[94])

        assertEquals('.', ruler[0])
        assertEquals('.', ruler[7])

        val expected =
            "....x....1....x....2....x....3....x....4....x....5" +
                "....x....6....x....7....x....8....x....9....x....0"
        assertEquals(expected, ruler)
    }
}
