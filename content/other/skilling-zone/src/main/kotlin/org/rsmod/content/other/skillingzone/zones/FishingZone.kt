package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.West
import org.rsmod.game.loc.LocShape.GroundDecor

val FishingZone =
    Zone(
        name = "fishing",
        walls = prifWalls(3293, 6036, 3310, 6051, Entrance(Side.North, start = 3300, width = 4)),
        resources = emptyList(),
        decorations =
            listOf(
                placed(REEDS, 3305, 6047, West),
                placed(REEDS, 3306, 6048, East),
                placed(REEDS, 3307, 6046, North),
                placed(RIPPLE, 3305, 6047, West, shape = GroundDecor),
                placed(RIPPLE, 3306, 6048, West, shape = GroundDecor),
                placed(RIPPLE, 3307, 6046, West, shape = GroundDecor),
            ),
        npcs =
            listOf(
                spawned(NET_BAIT, 3299, 6041),
                spawned(LURE_BAIT, 3301, 6043),
                spawned(CAGE_HARPOON, 3303, 6041),
                spawned(BIG_NET_HARPOON, 3301, 6039),
                spawned(LURE_BAIT, 3306, 6045),
                spawned(MONKFISH, 3304, 6047),
            ),
    )

private const val REEDS = "loc.tall_reeds_noop"
private const val RIPPLE = "loc.ripple"

private const val NET_BAIT = "npc.0_50_49_saltfish"
private const val LURE_BAIT = "npc.0_26_57_freshfish"
private const val CAGE_HARPOON = "npc.0_40_53_rarefish"
private const val BIG_NET_HARPOON = "npc.0_40_53_memberfish"
private const val MONKFISH = "npc.swan_fishingspot"
