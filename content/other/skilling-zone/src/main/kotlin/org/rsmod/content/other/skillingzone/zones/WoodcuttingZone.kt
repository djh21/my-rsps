package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

val WoodcuttingZone =
    Zone(
        name = "woodcutting",
        walls = prifWalls(3223, 6085, 3246, 6106, Entrance(Side.South, start = 3239, width = 4)),
        resources =
            listOf(
                placed(TREE, 3226, 6088, North, regrow = true),
                placed(TREE, 3236, 6089, East, regrow = true),
                placed(TREE, 3243, 6089, South, regrow = true),
                placed(TREE, 3237, 6094, West, regrow = true),
                placed(OAK, 3232, 6088, West, regrow = true),
                placed(OAK, 3228, 6091, South, regrow = true),
                placed(WILLOW, 3234, 6092, North, regrow = true),
                placed(WILLOW, 3241, 6093, East, regrow = true),
                placed(WILLOW, 3243, 6096, West, regrow = true),
                placed(WILLOW, 3225, 6101, South, regrow = true),
                placed(MAPLE, 3225, 6094, East, regrow = true),
                placed(MAPLE, 3240, 6096, North, regrow = true),
                placed(TEAK, 3237, 6098, South, regrow = true),
                placed(YEW, 3237, 6102, East, regrow = true),
                placed(YEW, 3242, 6102, North, regrow = true),
            ),
        decorations = emptyList(),
    )

private const val TREE = "loc.tree"
private const val OAK = "loc.oaktree"
private const val WILLOW = "loc.willowtree"
private const val MAPLE = "loc.mapletree"
private const val TEAK = "loc.teaktree"
private const val YEW = "loc.yewtree"
