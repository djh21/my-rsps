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
            List(RANGE_COUNT) {
                placed(RANGE, RANGE_LINE_X, FIRST_RANGE_Z + it * (RANGE_LENGTH + RANGE_GAP), West)
            },
        decorations =
            listOf(
                placed(BARREL, 3301, 6062, West),
                placed(BARREL, 3309, 6062, North),
                placed(BARREL, 3301, 6077, East),
                placed(BARREL, 3309, 6077, South),
                placed(FLOWERS, 3301, 6069, East),
                placed(FLOWERS, 3301, 6073, West),
                placed(FLOWERS_ALT, 3309, 6065, West),
                placed(FLOWERS, 3309, 6069, North),
                placed(FLOWERS_ALT, 3309, 6073, East),
            ),
    )

private const val RANGE = "loc.elf_village_range"
private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"

private const val RANGE_COUNT = 4
private const val RANGE_LINE_X = 3305
private const val FIRST_RANGE_Z = 6063
private const val RANGE_LENGTH = 2
private const val RANGE_GAP = 2
