package org.rsmod.content.other.skillingzone.zones

import org.rsmod.game.loc.LocAngle.East
import org.rsmod.game.loc.LocAngle.North
import org.rsmod.game.loc.LocAngle.West

private const val OVEN = "loc.potteryoven"
private const val POTTERY_WHEEL = "loc.potterywheel"
private const val FURNACE = "loc.prif_furnace"
private const val SANDPIT = "loc.prif_sandpit"
private const val SPINNING_WHEEL = "loc.elf_village_spinning_wheel"
private const val LOOM = "loc.regicide_loom"

val CraftingZone =
    Zone(
        name = "crafting",
        walls = prifWalls(3247, 6105, 3262, 6131, Entrance(Side.South, start = 3255, width = 4)),
        resources =
            listOf(
                placed(OVEN, 3253, 6129, North),
                placed(OVEN, 3256, 6129, North),
                placed(POTTERY_WHEEL, 3252, 6125, East),
                placed(POTTERY_WHEEL, 3250, 6123, East),
                placed(FURNACE, 3254, 6121, East),
                placed(FURNACE, 3254, 6123, East),
                placed(SANDPIT, 3250, 6119, East),
                placed(SPINNING_WHEEL, 3259, 6108, West),
                placed(SPINNING_WHEEL, 3260, 6114, West),
                placed(LOOM, 3260, 6123, West),
                placed(LOOM, 3259, 6128, West),
            ),
        decorations = emptyList(),
    )
