package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.West

private val SPOT_COLUMNS_X = listOf(3271, 3277, 3283)
private val SPOT_ROWS_Z = listOf(6041, 6037, 6033)

private val ORE_ROWS =
    listOf(
        listOf("loc.copperrock1", "loc.tinrock1", "loc.ironrock1"),
        listOf("loc.silverrock1", "loc.coalrock1", "loc.goldrock1"),
        listOf("loc.mithrilrock1", "loc.adamantiterock1", "loc.runiterock1"),
    )

val MiningZone =
    Zone(
        name = "mining",
        walls = prifWalls(3266, 6030, 3288, 6046, Entrance(Side.North, start = 3279, width = 4)),
        resources =
            ORE_ROWS.zip(SPOT_ROWS_Z).flatMap { (ores, z) ->
                ores.zip(SPOT_COLUMNS_X).flatMap { (ore, x) -> spot(ore, x, z) }
            },
        decorations = emptyList(),
    )

private fun spot(rock: String, standX: Int, standZ: Int): List<ZoneLoc> =
    listOf(
        placed(rock, standX - 1, standZ, West),
        placed(rock, standX, standZ + 1, North),
        placed(rock, standX + 1, standZ, East),
    )
