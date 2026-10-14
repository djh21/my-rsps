package org.rsmod.content.other.skillingzone.zones

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocEntity
import org.rsmod.game.loc.LocInfo
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.loc.LocLayerConstants

data class Zone(
    val name: String,
    val walls: List<ZoneLoc>,
    val resources: List<ZoneLoc>,
    val decorations: List<ZoneLoc>,
) {
    val spawns: List<ZoneLoc>
        get() = walls + resources + decorations
}

data class ZoneLoc(
    val loc: String,
    val coords: CoordGrid,
    val shape: LocShape,
    val angle: LocAngle,
    val regrow: Boolean = false,
) {
    fun toLocInfo(): LocInfo {
        val entity = LocEntity(loc.asRSCM(RSCMType.LOC), shape.id, angle.id)
        return LocInfo(LocLayerConstants.of(shape.id), coords, entity)
    }
}

internal const val ZONE_LEVEL = 0

internal fun placed(
    loc: String,
    x: Int,
    z: Int,
    angle: LocAngle,
    regrow: Boolean = false,
) = ZoneLoc(loc, CoordGrid(x, z, ZONE_LEVEL), LocShape.CentrepieceStraight, angle, regrow)
