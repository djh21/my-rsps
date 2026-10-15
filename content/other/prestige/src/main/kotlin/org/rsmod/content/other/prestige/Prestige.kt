package org.rsmod.content.other.prestige

import dev.openrune.ServerCacheManager
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import kotlin.math.max
import org.rsmod.annotations.InternalApi
import org.rsmod.api.player.stat.PlayerSkillXP
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.player.stat.statSub
import org.rsmod.api.player.ui.PlayerInterfaceUpdates
import org.rsmod.api.player.vars.VarPlayerIntMapSetter
import org.rsmod.api.table.PrestigeBonusesRow
import org.rsmod.game.entity.Player
import org.rsmod.game.stat.PlayerSkillXPTable

private const val STAT_PREFIX = "stat."
private const val RESET_SPOTANIM = "spotanim.levelup_99_anim"

enum class PrestigeOutcome {
    Success,
    BelowRequiredLevel,
    MaxPrestige,
}

val PrestigeBonusesRow.shortName: String
    get() = stat.removePrefix(STAT_PREFIX)

val PrestigeBonusesRow.displayName: String
    get() = shortName.replaceFirstChar { it.uppercase() }

fun PrestigeBonusesRow.Companion.byName(name: String): PrestigeBonusesRow? {
    val key = name.trim().lowercase().removePrefix(STAT_PREFIX)
    return all().firstOrNull { it.shortName == key }
}

fun Player.prestigeLevel(row: PrestigeBonusesRow): Int =
    vars["varbit.prestige_${row.shortName}"]

fun Player.prestigeMultiplier(row: PrestigeBonusesRow): Double =
    PrestigeMath.multiplier(prestigeLevel(row), row.bonusPercent, row.maxPrestige)

fun Player.prestigeBonusPercent(row: PrestigeBonusesRow): Int =
    prestigeLevel(row).coerceAtMost(row.maxPrestige) * row.bonusPercent

@OptIn(InternalApi::class)
fun Player.prestige(row: PrestigeBonusesRow): PrestigeOutcome {
    val prestige = prestigeLevel(row)
    if (prestige >= row.maxPrestige.coerceAtMost(PrestigeMath.MAX_STORABLE_PRESTIGE)) {
        return PrestigeOutcome.MaxPrestige
    }
    if (!PrestigeMath.canPrestige(statBase(row.stat), prestige, row.maxPrestige)) {
        return PrestigeOutcome.BelowRequiredLevel
    }
    resetStat(row.stat)
    VarPlayerIntMapSetter.set(this, "varbit.prestige_${row.shortName}", prestige + 1)
    spotanim(RESET_SPOTANIM)
    return PrestigeOutcome.Success
}

@OptIn(InternalApi::class)
private fun Player.resetStat(stat: String) {
    val type =
        ServerCacheManager.getStats(stat.asRSCM(RSCMType.STAT)) ?: error("No stat found for $stat")
    val targetLevel = max(type.minLevel, 1)
    statMap.setCurrentLevel(stat, statMap.getBaseLevel(stat))
    val levelDelta = statBase(stat) - targetLevel
    statMap.setXP(stat, PlayerSkillXPTable.getXPFromLevel(targetLevel))
    statMap.setBaseLevel(stat, targetLevel.toByte())
    statSub(stat, constant = levelDelta, percent = 0)
    appearance.combatLevel = PlayerSkillXP.calculateCombatLevel(this)
    PlayerInterfaceUpdates.updateCombatLevel(this)
}
