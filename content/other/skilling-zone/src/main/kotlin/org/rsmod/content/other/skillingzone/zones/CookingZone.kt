package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

val CookingZone =
    Zone(
        name = "cooking",
        walls = prifWalls(3299, 6060, 3311, 6079, Entrance(Side.West, start = 6064, width = 4)),
        resources =
            listOf(
                placed(RANGE, 3304, 6065, West),
                placed(RANGE, 3307, 6068, North),
                placed(RANGE, 3303, 6074, East),
                placed(RANGE, 3307, 6075, South),
            ),
        decorations =
            listOf(
                placed(BARREL, 3302, 6077, West),
                placed(BARREL, 3309, 6070, North),
                placed(FLOWERS, 3301, 6062, East),
                placed(FLOWERS_ALT, 3306, 6062, West),
                placed(FLOWERS, 3309, 6066, North),
                placed(FLOWERS_ALT, 3305, 6077, South),
            ),
    )

private const val RANGE = "loc.elf_village_range"
private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"
