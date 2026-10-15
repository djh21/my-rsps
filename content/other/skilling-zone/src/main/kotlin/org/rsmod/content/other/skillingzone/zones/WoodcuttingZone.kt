package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.South
import org.rsmod.game.loc.LocAngle.West

val WoodcuttingZone =
    Zone(
        name = "woodcutting",
        walls = prifWalls(3223, 6085, 3246, 6106, Entrance(Side.East, start = 6087, width = 4)),
        resources =
            listOf(
                placed(TREE, 3226, 6088, North),
                placed(TREE, 3236, 6089, East),
                placed(TREE, 3243, 6092, South),
                placed(TREE, 3237, 6094, West),
                placed(OAK, 3232, 6088, West),
                placed(OAK, 3228, 6091, South),
                placed(WILLOW, 3234, 6092, North),
                placed(WILLOW, 3241, 6093, East),
                placed(WILLOW, 3243, 6096, West),
                placed(WILLOW, 3225, 6101, South),
                placed(MAPLE, 3225, 6094, East),
                placed(MAPLE, 3240, 6096, North),
                placed(TEAK, 3237, 6098, South),
                placed(YEW, 3237, 6102, East),
                placed(YEW, 3242, 6102, North),
            ),
        decorations = emptyList(),
    )

private const val TREE = "loc.tree"
private const val OAK = "loc.oaktree"
private const val WILLOW = "loc.willowtree"
private const val MAPLE = "loc.mapletree"
private const val TEAK = "loc.teaktree"
private const val YEW = "loc.yewtree"
