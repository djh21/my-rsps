package org.rsmod.content.skills.runecrafting.essence

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.player.output.ChatType
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.script.onGameStartup
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.entity.Npc
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocEntity
import org.rsmod.game.loc.LocInfo
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.loc.LocLayerConstants

class EssenceMineScript
@Inject
constructor(private val locRepo: LocRepository, private val npcRepo: NpcRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onGameStartup {
            place(BANK_BOOTH, 2908, 4826)
            place(BANK_BOOTH, 2909, 4826)
            place(RETURN_PORTAL, 2912, 4826)
            npcRepo.add(Npc(BANKER, CoordGrid(2908, 4825)), Int.MAX_VALUE)
        }

        onOpLoc1(RETURN_PORTAL) {
            mes("You step through the portal...", ChatType.Spam)
            telejump(RETURN_DESTINATION)
            soundSynth("synth.teleport_all")
        }
    }

    private fun place(loc: String, x: Int, z: Int) {
        val shape = LocShape.CentrepieceStraight
        val entity = LocEntity(loc.asRSCM(RSCMType.LOC), shape.id, LocAngle.West.id)
        locRepo.add(LocInfo(LocLayerConstants.of(shape.id), CoordGrid(x, z), entity), Int.MAX_VALUE)
    }

    private companion object {
        const val BANK_BOOTH = "loc.bankbooth"
        const val RETURN_PORTAL = "loc.prifddinas_portal"
        const val BANKER = "npc.banker1_west"
        val RETURN_DESTINATION = CoordGrid(3297, 6107, 0)
    }
}
