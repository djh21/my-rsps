package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

private const val FURNACE = "loc.prif_furnace"
private const val ANVIL = "loc.anvil"
private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"

private const val STATION_COUNT = 5
private const val FIRST_STATION_X = 3273
private const val STATION_GAP = 2
private const val FURNACE_Z = 6060
private const val ANVIL_Z = 6055

val SmithingZone =
    Zone(
        name = "smithing",
        walls = prifWalls(3266, 6052, 3288, 6063, Entrance(Side.West, start = 6056, width = 4)),
        resources =
            List(STATION_COUNT) {
                placed(FURNACE, FIRST_STATION_X + it * STATION_GAP, FURNACE_Z, South)
            } +
                List(STATION_COUNT) {
                    placed(ANVIL, FIRST_STATION_X + it * STATION_GAP, ANVIL_Z, North)
                },
        decorations =
            listOf(
                placed(BARREL, 3269, 6054, West),
                placed(BARREL, 3285, 6054, North),
                placed(BARREL, 3269, 6061, South),
                placed(BARREL, 3285, 6061, East),
                placed(FLOWERS, 3271, 6054, West),
                placed(FLOWERS_ALT, 3283, 6054, East),
                placed(FLOWERS_ALT, 3271, 6061, West),
                placed(FLOWERS, 3283, 6061, East),
            ),
    )
