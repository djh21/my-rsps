package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

val CookingZone =
    Zone(
        name = "cooking",
        cleared =
            listOf(
                scenery(BUSH, 3277, 6071),
                scenery(LAMP, 3274, 6068),
                scenery(PRIF_TREE_SMALL, 3274, 6069),
                scenery(PRIF_TREE_SMALL, 3275, 6071),
                scenery(PRIF_TREE_SMALL, 3277, 6069),
                scenery(PRIF_TREE_SMALL, 3278, 6071),
                scenery(PRIF_TREE_LARGE, 3271, 6068),
                scenery(PRIF_TREE_LARGE, 3276, 6073),
            ),
        fence =
            listOf(
                wall(3273, 6071, West),
                wall(3275, 6073, North),
                wall(3278, 6070, East),
                wall(3278, 6071, East),
                wall(3278, 6072, East),
                wall(3279, 6074, East),
                wall(3274, 6068, South),
                wall(3275, 6068, South),
                wall(3276, 6068, South),
                corner(3271, 6069, West),
                corner(3272, 6070, West),
                corner(3276, 6074, West),
                corner(3277, 6075, West),
                corner(3273, 6067, East),
                corner(3277, 6068, East),
                corner(3278, 6069, East),
                corner(3279, 6073, East),
            ),
        resources =
            listOf(
                placed(RANGE, 3273, 6071, North),
                placed(RANGE, 3277, 6070, East),
                placed(FIRE, 3277, 6075, East, regrow = true),
                placed(FIRE, 3275, 6073, North, regrow = true),
                placed(FIRE, 3277, 6068, South, regrow = true),
            ),
        decorations =
            listOf(
                placed(LAMP, 3273, 6067, North),
                placed(BUSH, 3272, 6070, North),
                placed(BUSH, 3276, 6074, North),
                placed(BUSH, 3278, 6069, North),
                placed(BUSH, 3279, 6073, South),
                placed(BLUE_FLOWERS, 3276, 6068, East),
                placed(BLUE_FLOWERS, 3276, 6069, South),
                placed(FLOWERS_1, 3275, 6068, East),
                placed(FLOWERS_1, 3277, 6072, North),
                placed(FLOWERS_1, 3278, 6075, South),
                placed(FLOWERS_2, 3275, 6070, North),
                placed(FLOWERS_3, 3277, 6069, North),
                placed(TULIPS, 3274, 6070, North),
            ),
    )

private const val RANGE = "loc.range"
private const val FIRE = "loc.fire"
private const val LAMP = "loc.prif_crystal_lamp"
private const val BUSH = "loc.plants_update_small_bush_grassland"
private const val BLUE_FLOWERS = "loc.rareflowers_blue"
private const val FLOWERS_1 = "loc.regicide_rareflowers_1"
private const val FLOWERS_2 = "loc.regicide_rareflowers_2"
private const val FLOWERS_3 = "loc.regicide_rareflowers_3"
private const val TULIPS = "loc.tulips"
private const val PRIF_TREE_SMALL = "loc.prif_tree_normal_1"
private const val PRIF_TREE_LARGE = "loc.prif_tree_normal_2"
