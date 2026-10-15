package org.rsmod.content.skills.runecrafting

import dev.openrune.ServerCacheManager
import dev.openrune.filesystem.Cache
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.parallel.Execution
import org.junit.jupiter.api.parallel.ExecutionMode
import org.junit.jupiter.api.parallel.ResourceLock
import org.rsmod.api.table.runecrafting.RunecraftingRunesRow
import org.rsmod.content.skills.runecrafting.action.OuraniaAltar
import org.rsmod.content.skills.runecrafting.action.OuraniaAltar.Candidate

@Execution(ExecutionMode.SAME_THREAD)
@ResourceLock("ServerCacheManager")
class OuraniaAltarTest {
    private val pure = "obj.blankrune_high"

    private val candidates =
        listOf(
            Candidate("obj.airrune", 1, 5, setOf(pure)),
            Candidate("obj.mindrune", 2, 5, setOf(pure)),
            Candidate("obj.waterrune", 5, 6, setOf(pure)),
            Candidate("obj.cosmicrune", 27, 8, setOf(pure)),
            Candidate("obj.sunfirerune", 33, 9, setOf(pure)),
            Candidate("obj.soulrune", 90, 30, setOf(pure)),
        )

    private fun split(total: Int, level: Int, input: String = pure): Map<String, Int> {
        val eligible = OuraniaAltar.eligible(candidates, level, input)
        val counts = OuraniaAltar.apportion(total, eligible.map { OuraniaAltar.weight(it.level) })
        return eligible.indices.associate { eligible[it].output to counts[it] }
    }

    @Test
    fun `counts always sum to the essence total`() {
        for (level in listOf(1, 5, 27, 99)) {
            for (total in listOf(0, 1, 2, 3, 7, 27, 28, 100, 1_000, 123_457)) {
                assertEquals(total, split(total, level).values.sum(), "level=$level total=$total")
            }
        }
    }

    @Test
    fun `level one player receives only air`() {
        assertEquals(mapOf("obj.airrune" to 28), split(28, 1))
    }

    @Test
    fun `fewer essence than eligible runes still conserves and never goes negative`() {
        val counts = split(2, 99)
        assertEquals(2, counts.values.sum())
        assertTrue(counts.values.all { it >= 0 })
        assertEquals(1, counts.getValue("obj.airrune"))
        assertEquals(1, counts.getValue("obj.mindrune"))
    }

    @Test
    fun `lower level runes never receive fewer than higher level ones`() {
        for (total in 1..400) {
            val counts = split(total, 99).values.toList()
            assertTrue(counts.zipWithNext().all { (a, b) -> a >= b }, "total=$total $counts")
        }
    }

    @Test
    fun `weights strictly decrease with level`() {
        assertTrue(OuraniaAltar.weight(1) > OuraniaAltar.weight(2))
        assertTrue(OuraniaAltar.weight(2) > OuraniaAltar.weight(99))
    }

    @Test
    fun `eligibility is gated by level and excluded runes and input type`() {
        val atTwenty = OuraniaAltar.eligible(candidates, 20, pure).map { it.output }
        assertEquals(listOf("obj.airrune", "obj.mindrune", "obj.waterrune"), atTwenty)

        val atMax = OuraniaAltar.eligible(candidates, 99, pure).map { it.output }
        assertFalse("obj.sunfirerune" in atMax)
        assertFalse("obj.soulrune" in atMax)
        assertTrue("obj.cosmicrune" in atMax)

        assertTrue(OuraniaAltar.eligible(candidates, 99, "obj.blankrune").isEmpty())
    }

    @Test
    fun `empty weight list yields nothing`() {
        assertEquals(0, OuraniaAltar.apportion(10, emptyList()).size)
    }

    @Test
    fun `real rune table excludes banned runes`() {
        val level = 99
        val rows = RunecraftingRunesRow.all()
        val real =
            rows.map {
                Candidate(
                    it.output.internalName,
                    it.statReq.first().t1,
                    it.xp,
                    it.input.map { item -> item.internalName }.toSet(),
                )
            }
        val pureOutputs = OuraniaAltar.eligible(real, level, pure).map { it.output }
        assertTrue("obj.airrune" in pureOutputs)
        assertTrue("obj.naturerune" in pureOutputs)
        assertFalse("obj.sunfirerune" in pureOutputs)
        assertFalse("obj.wrathrune" in pureOutputs)
        assertFalse("obj.aetherrune" in pureOutputs)
        assertFalse("obj.soulrune" in pureOutputs)
        assertTrue(OuraniaAltar.eligible(real, level, "obj.blankrune").isNotEmpty())
        val counts = OuraniaAltar.apportion(28, pureOutputs.indices.map { OuraniaAltar.weight(it) })
        assertEquals(28, counts.sum())
    }

    companion object {
        private lateinit var cache: Cache

        @JvmStatic
        @BeforeAll
        fun open() {
            cache = ServerCacheManager.init(240)
        }

        @JvmStatic
        @AfterAll
        fun close() {
            cache.close()
        }
    }
}
