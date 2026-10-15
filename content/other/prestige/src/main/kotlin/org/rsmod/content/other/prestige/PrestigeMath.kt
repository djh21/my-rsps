package org.rsmod.content.other.prestige

object PrestigeMath {
    const val REQUIRED_LEVEL = 99
    const val MAX_STORABLE_PRESTIGE = 31

    fun multiplier(prestige: Int, bonusPercent: Int, maxPrestige: Int): Double {
        val capped = prestige.coerceIn(0, maxPrestige.coerceIn(0, MAX_STORABLE_PRESTIGE))
        return (100 + bonusPercent * capped) / 100.0
    }

    fun canPrestige(baseLevel: Int, prestige: Int, maxPrestige: Int): Boolean =
        baseLevel >= REQUIRED_LEVEL && prestige < maxPrestige.coerceAtMost(MAX_STORABLE_PRESTIGE)
}
