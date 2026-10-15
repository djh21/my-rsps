package org.rsmod.content.interfaces.xpboard

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class HighscorePagingTest {
    private fun rows(count: Int) =
        rankRows(List(count) { RawHighscore("p$it", 99, 2_000_000_000L - it) })

    @Test
    fun `ranks follow query order starting at one and xp drops the fine digit`() {
        val ranked = rankRows(listOf(RawHighscore("a", 99, 130_344_310L), RawHighscore("b", 98, 5)))
        assertEquals(listOf(1, 2), ranked.map { it.rank })
        assertEquals(13_034_431L, ranked[0].xp)
        assertEquals(0L, ranked[1].xp)
    }

    @Test
    fun `page count rounds up and is never zero`() {
        assertEquals(1, pageCount(0))
        assertEquals(1, pageCount(15))
        assertEquals(2, pageCount(16))
        assertEquals(7, pageCount(100))
    }

    @Test
    fun `page numbers are clamped into range`() {
        assertEquals(0, clampPage(-3, 100))
        assertEquals(6, clampPage(99, 100))
        assertEquals(0, clampPage(5, 0))
    }

    @Test
    fun `slices cover every row exactly once with a short last page`() {
        val all = rows(100)
        val pages = (0 until pageCount(all.size)).map { pageSlice(all, it) }
        assertEquals(all, pages.flatten())
        assertEquals(PAGE_SIZE, pages.first().size)
        assertEquals(10, pages.last().size)
        assertTrue(pageSlice(emptyList(), 0).isEmpty())
    }

    @Test
    fun `xp is grouped with commas`() {
        assertEquals("200,000,000", formatXp(200_000_000L))
        assertEquals("0", formatXp(0L))
    }
}
