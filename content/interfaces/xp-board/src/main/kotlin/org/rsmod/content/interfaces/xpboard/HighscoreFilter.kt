package org.rsmod.content.interfaces.xpboard

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType

internal class HighscoreFilter(val title: String, val statId: Int?)

internal object HighscoreFilters {
    private val SKILLS =
        listOf(
            "attack" to "Attack",
            "defence" to "Defence",
            "strength" to "Strength",
            "hitpoints" to "Hitpoints",
            "ranged" to "Ranged",
            "prayer" to "Prayer",
            "magic" to "Magic",
            "cooking" to "Cooking",
            "woodcutting" to "Woodcutting",
            "fletching" to "Fletching",
            "fishing" to "Fishing",
            "firemaking" to "Firemaking",
            "crafting" to "Crafting",
            "smithing" to "Smithing",
            "mining" to "Mining",
            "herblore" to "Herblore",
            "agility" to "Agility",
            "thieving" to "Thieving",
            "slayer" to "Slayer",
            "farming" to "Farming",
            "runecrafting" to "Runecrafting",
            "hunter" to "Hunter",
            "construction" to "Construction",
            "sailing" to "Sailing",
        )

    val TOTAL = HighscoreFilter("Total", null)

    val ALL: List<HighscoreFilter> by lazy {
        listOf(TOTAL) +
            SKILLS.map { (symbol, title) ->
                HighscoreFilter(title, "stat.$symbol".asRSCM(RSCMType.STAT))
            }
    }

    val SKILL_STAT_IDS: List<Int> by lazy { ALL.mapNotNull { it.statId } }
}
