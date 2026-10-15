package org.rsmod.content.other.prestige

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class PrestigeMathTest {
    @Test
    fun `no prestige leaves xp unchanged`() {
        assertEquals(1.0, PrestigeMath.multiplier(0, 10, 10))
    }

    @Test
    fun `bonus scales per prestige level`() {
        assertEquals(1.1, PrestigeMath.multiplier(1, 10, 10))
        assertEquals(1.5, PrestigeMath.multiplier(5, 10, 10))
        assertEquals(1.25, PrestigeMath.multiplier(5, 5, 10))
    }

    @Test
    fun `prestige above the cap is clamped`() {
        assertEquals(2.0, PrestigeMath.multiplier(10, 10, 10))
        assertEquals(2.0, PrestigeMath.multiplier(31, 10, 10))
    }

    @Test
    fun `negative prestige and zero max are safe`() {
        assertEquals(1.0, PrestigeMath.multiplier(-3, 10, 10))
        assertEquals(1.0, PrestigeMath.multiplier(4, 10, 0))
    }

    @Test
    fun `scaled xp rounds consistently`() {
        assertEquals(133.0, 100.0 * PrestigeMath.multiplier(3, 11, 10), 1e-9)
        assertEquals(1000.0, 1000.0 * PrestigeMath.multiplier(0, 10, 10), 0.0)
    }

    @Test
    fun `prestige requires level 99 and room below the cap`() {
        assertTrue(PrestigeMath.canPrestige(99, 0, 10))
        assertFalse(PrestigeMath.canPrestige(98, 0, 10))
        assertFalse(PrestigeMath.canPrestige(99, 10, 10))
        assertTrue(PrestigeMath.canPrestige(99, 9, 10))
        assertFalse(PrestigeMath.canPrestige(99, 31, 99))
    }
}
