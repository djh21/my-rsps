package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"

val FiremakingZone =
    Zone(
        name = "firemaking",
        walls = prifWalls(3267, 6119, 3293, 6132, Entrance(Side.South, start = 3271, width = 4)),
        resources = emptyList(),
        decorations =
            listOf(
                placed(BARREL, 3270, 6122, West),
                placed(FLOWERS_ALT, 3275, 6122, South),
                placed(FLOWERS, 3270, 6124, West),
                placed(BARREL, 3270, 6129, South),
                placed(FLOWERS_ALT, 3273, 6130, West),
                placed(FLOWERS, 3275, 6130, North),
                placed(FLOWERS, 3279, 6121, South),
                placed(BARREL, 3283, 6122, North),
                placed(FLOWERS, 3284, 6130, East),
                placed(BARREL, 3288, 6130, North),
                placed(FLOWERS, 3288, 6121, East),
                placed(BARREL, 3290, 6122, East),
                placed(FLOWERS_ALT, 3291, 6125, North),
                placed(FLOWERS_ALT, 3291, 6128, West),
            ),
    )
