package org.rsmod.content.other.prestige.pack

import dev.openrune.definition.dbtables.dbTable
import dev.openrune.definition.util.VarType

object PrestigeTable {

    const val STAT = 0
    const val BONUS_PERCENT = 1
    const val MAX_PRESTIGE = 2

    private const val DEFAULT_BONUS_PERCENT = 10
    private const val DEFAULT_MAX_PRESTIGE = 10

    private val STATS =
        listOf(
            "attack",
            "defence",
            "strength",
            "hitpoints",
            "ranged",
            "prayer",
            "magic",
            "cooking",
            "woodcutting",
            "fletching",
            "fishing",
            "firemaking",
            "crafting",
            "smithing",
            "mining",
            "herblore",
            "agility",
            "thieving",
            "slayer",
            "farming",
            "runecrafting",
            "hunter",
            "construction",
            "sailing",
        )

    private val BONUS_PERCENT_OVERRIDES = mapOf<String, Int>()
    private val MAX_PRESTIGE_OVERRIDES = mapOf<String, Int>()

    fun bonuses() =
        dbTable("dbtable.prestige_bonuses", serverOnly = true) {
            column("stat", STAT, VarType.STRING)
            column("bonus_percent", BONUS_PERCENT, VarType.INT)
            column("max_prestige", MAX_PRESTIGE, VarType.INT)
            for (stat in STATS) {
                row("dbrow.prestige_$stat") {
                    column(STAT, "stat.$stat")
                    column(BONUS_PERCENT, BONUS_PERCENT_OVERRIDES[stat] ?: DEFAULT_BONUS_PERCENT)
                    column(MAX_PRESTIGE, MAX_PRESTIGE_OVERRIDES[stat] ?: DEFAULT_MAX_PRESTIGE)
                }
            }
        }
}
