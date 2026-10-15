package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

private const val SEED_STALL = "loc.seed_stall"
private const val RAG_STALL = "loc.rag_market_stall"
private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"

private const val STALL_COUNT = 5
private const val FIRST_STALL_X = 3226
private const val STALL_GAP = 5
private const val STALL_Z = 6035

val ThievingZone =
    Zone(
        name = "thieving",
        walls = prifWalls(3220, 6029, 3252, 6049, Entrance(Side.North, start = 3235, width = 4)),
        resources =
            List(STALL_COUNT) {
                val stall = if (it % 2 == 0) SEED_STALL else RAG_STALL
                placed(stall, FIRST_STALL_X + it * STALL_GAP, STALL_Z, North)
            },
        decorations =
            listOf(
                placed(BARREL, 3223, 6032, West),
                placed(BARREL, 3249, 6032, North),
                placed(BARREL, 3223, 6046, South),
                placed(BARREL, 3249, 6046, East),
                placed(FLOWERS, 3225, 6032, East),
                placed(FLOWERS_ALT, 3247, 6032, West),
                placed(FLOWERS_ALT, 3225, 6046, East),
                placed(FLOWERS, 3247, 6046, West),
                placed(FLOWERS, 3223, 6035, East),
                placed(FLOWERS_ALT, 3249, 6035, West),
                placed(FLOWERS, 3231, 6046, East),
                placed(FLOWERS_ALT, 3243, 6046, West),
            ),
        npcs =
            listOf(
                spawned("npc.man", 3226, 6040),
                spawned("npc.woman", 3245, 6042),
                spawned("npc.man2", 3231, 6042),
                spawned("npc.woman2", 3246, 6038),
                spawned("npc.man3", 3238, 6032),
                spawned("npc.woman3", 3229, 6032),
                spawned("npc.master_farmer_1", 3240, 6040),
                spawned("npc.man", 3241, 6042),
            ),
    )
