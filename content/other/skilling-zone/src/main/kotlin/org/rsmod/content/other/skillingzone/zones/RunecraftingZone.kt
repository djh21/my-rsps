package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

val RunecraftingZone =
    Zone(
        name = "runecrafting",
        walls = prifWalls(3284, 6084, 3313, 6118, Entrance(Side.South, start = 3296, width = 4)),
        resources = emptyList(),
        decorations =
            listOf(
                placed(PORTAL, 3297, 6109, West),
                placed(MIND_PORTAL, 3302, 6109, West),
                placed(ALTAR_SARADOMIN, 3307, 6105, North),
                placed(ALTAR_ZAMORAK, 3307, 6098, North),
                placed(ALTAR_GUTHIX, 3307, 6091, North),
                placed(STATUE_SARADOMIN, 3310, 6105, West),
                placed(STATUE_ZAMORAK, 3310, 6098, West),
                placed(STATUE_GUTHIX, 3310, 6091, West),
                placed(LAMP, 3295, 6087, West),
                placed(LAMP, 3300, 6087, West),
                placed(LAMP, 3295, 6109, West),
                placed(LAMP, 3300, 6109, West),
                placed(BARREL, 3286, 6086, West),
                placed(BARREL, 3310, 6086, North),
                placed(BARREL, 3286, 6116, East),
                placed(BARREL, 3310, 6116, South),
                placed(FLOWERS, 3293, 6111, East),
                placed(FLOWERS_ALT, 3302, 6112, West),
                placed(FLOWERS_ALT, 3295, 6092, North),
                placed(FLOWERS, 3301, 6100, South),
                placed(FLOWERS, 3302, 6090, West),
                placed(FLOWERS_ALT, 3292, 6113, East),
                placed(FLOWERS, 3309, 6112, North),
                placed(FLOWERS_ALT, 3304, 6108, South),
                placed(FLOWERS, 3304, 6094, East),
                placed(FLOWERS, 3288, 6096, East),
                placed(FLOWERS_ALT, 3291, 6100, North),
                placed(FLOWERS, 3288, 6104, South),
            ),
    )

private const val PORTAL = "loc.poh_portal_teak_ourania"
private const val MIND_PORTAL = "loc.pure_essence_portal"
private const val ALTAR_SARADOMIN = "loc.poh_altar_saradomin_3"
private const val ALTAR_ZAMORAK = "loc.poh_altar_zamorak_3"
private const val ALTAR_GUTHIX = "loc.poh_altar_guthix_3"
private const val STATUE_SARADOMIN = "loc.poh_statue_saradomin"
private const val STATUE_ZAMORAK = "loc.poh_statue_zamorak"
private const val STATUE_GUTHIX = "loc.poh_statue_guthix"
private const val LAMP = "loc.prif_crystal_lamp"
private const val BARREL = "loc.fai_varrock_feather_barrel"
private const val FLOWERS = "loc.regicide_rareflowers_3"
private const val FLOWERS_ALT = "loc.regicide_rareflowers_2"
