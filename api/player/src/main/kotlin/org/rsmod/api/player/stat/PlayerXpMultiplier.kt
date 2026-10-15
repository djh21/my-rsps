package org.rsmod.api.player.stat

import org.rsmod.game.entity.Player

public fun interface PlayerXpMultiplier {
    public fun multiplier(player: Player, stat: String): Double
}

public object PlayerXpMultipliers {
    @Volatile public var provider: PlayerXpMultiplier? = null

    internal fun of(player: Player, stat: String): Double =
        provider?.multiplier(player, stat) ?: 1.0
}
