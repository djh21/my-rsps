package org.rsmod.content.skills.runecrafting.action

import dev.openrune.rscm.RSCM.asRSCM
import kotlin.math.floor
import org.rsmod.api.invtx.add
import org.rsmod.api.invtx.delete
import org.rsmod.api.invtx.invTransaction
import org.rsmod.api.invtx.select
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.stat.baseRunecraftingLvl
import org.rsmod.api.stats.xpmod.XpModifiers
import org.rsmod.api.table.runecrafting.RunecraftingRunesRow
import org.rsmod.content.skills.runecrafting.action.RunecraftAction.DAEYALT_XP_MULTIPLIER
import org.rsmod.content.skills.runecrafting.action.RunecraftAction.OURANIA_XP_MULTIPLIER
import org.rsmod.content.skills.runecrafting.action.RunecraftAction.advanceRunecraftingXp
import org.rsmod.content.skills.runecrafting.action.RunecraftAction.getBonusMultiplier
import org.rsmod.content.skills.runecrafting.action.RunecraftAction.preCraft
import org.rsmod.content.skills.runecrafting.essencepouch.EssencePouch
import org.rsmod.content.skills.runecrafting.items.RaimentsOfTheEye.applyBonus

object OuraniaAltar {
    private const val WEIGHT_STEP = 0.01

    private val EXCLUDED_RUNES =
        setOf("obj.sunfirerune", "obj.wrathrune", "obj.aetherrune", "obj.soulrune")

    class Candidate(
        val output: String,
        val level: Int,
        val xp: Int,
        val inputs: Set<String>,
    )

    private class EssenceGroup(
        val items: List<String>,
        val input: String,
        val xpFactor: Double,
    )

    private class Batch(val group: EssenceGroup, val counts: List<Int>, val total: Int)

    private val GROUPS =
        listOf(
            EssenceGroup(
                listOf(EssencePouch.PURE_ESSENCE, EssencePouch.GUARDIAN_ESSENCE),
                EssencePouch.PURE_ESSENCE,
                1.0,
            ),
            EssenceGroup(
                listOf(EssencePouch.DAEYALT_ESSENCE),
                EssencePouch.PURE_ESSENCE,
                DAEYALT_XP_MULTIPLIER,
            ),
        )

    fun weight(level: Int): Double = 1.0 / (1.0 + WEIGHT_STEP * level)

    fun eligible(
        candidates: List<Candidate>,
        playerLevel: Int,
        essenceInput: String,
    ): List<Candidate> =
        candidates
            .filter {
                it.output !in EXCLUDED_RUNES &&
                    it.level <= playerLevel &&
                    essenceInput in it.inputs
            }
            .sortedWith(compareBy({ it.level }, { it.output }))

    fun apportion(total: Int, weights: List<Double>): IntArray {
        val result = IntArray(weights.size)
        if (total <= 0 || weights.isEmpty()) {
            return result
        }
        val sum = weights.sum()
        val remainders = DoubleArray(weights.size)
        var assigned = 0
        for (i in weights.indices) {
            val quota = total * weights[i] / sum
            result[i] = floor(quota).toInt()
            remainders[i] = quota - result[i]
            assigned += result[i]
        }
        val order = weights.indices.sortedWith(compareBy({ -remainders[it] }, { it }))
        var leftover = total - assigned
        var cursor = 0
        while (leftover > 0) {
            result[order[cursor % order.size]]++
            cursor++
            leftover--
        }
        return result
    }

    suspend fun ProtectedAccess.craftOurania(xpMods: XpModifiers) {
        val level = player.baseRunecraftingLvl
        val candidates = RunecraftingRunesRow.all().map(::candidate)

        val batches =
            GROUPS.mapNotNull { group ->
                val total = group.items.sumOf { inv.physicalCount(it) }
                if (total <= 0) {
                    return@mapNotNull null
                }
                val runes = eligible(candidates, level, group.input)
                if (runes.isEmpty()) {
                    return@mapNotNull null
                }
                Batch(group, apportion(total, runes.map { weight(it.level) }).toList(), total)
            }
        if (batches.isEmpty()) {
            mesbox("You do not have any essence to bind.")
            return
        }

        val produced = LinkedHashMap<String, Int>()
        var baseXp = 0.0
        for (batch in batches) {
            val runes = eligible(candidates, level, batch.group.input)
            for ((index, rune) in runes.withIndex()) {
                val count = batch.counts[index]
                if (count <= 0) {
                    continue
                }
                val multiplier = getBonusMultiplier(rune.output, level).toInt()
                produced.merge(rune.output, applyBonus(count * multiplier), Int::plus)
                baseXp += count * multiplier * rune.xp * batch.group.xpFactor
            }
        }

        val result =
            player.invTransaction(inv) {
                val target = select(inv)
                for (batch in batches) {
                    for (item in batch.group.items) {
                        val held = inv.physicalCount(item)
                        if (held > 0) {
                            delete(target, item.asRSCM(), held)
                        }
                    }
                }
                for ((output, count) in produced) {
                    add(target, output.asRSCM(), count)
                }
            }
        if (!result.success) {
            mes("You do not have enough inventory space to bind these runes.")
            return
        }

        preCraft()
        advanceRunecraftingXp(baseXp * OURANIA_XP_MULTIPLIER, xpMods)
    }

    private fun candidate(row: RunecraftingRunesRow): Candidate =
        Candidate(
            output = row.output.internalName,
            level = row.statReq.first().t1,
            xp = row.xp,
            inputs = row.input.map { it.internalName }.toSet(),
        )
}
