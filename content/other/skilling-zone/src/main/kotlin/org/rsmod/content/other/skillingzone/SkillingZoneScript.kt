package org.rsmod.content.other.skillingzone

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onGameStartup
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillingZoneScript
@Inject
constructor(
    private val protectedAccess: ProtectedAccessLauncher,
    private val locRepo: LocRepository,
) : PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        onGameStartup { clearStairs() }
        onCommand("skilling") {
            desc = "Teleport to the Prifddinas skilling zone"
            cheat { protectedAccess.launch(player) { telejump(ENTRANCE, TeleportType.Exempt) } }
        }
    }

    private fun clearStairs() {
        val missing = mutableListOf<String>()
        for ((symbol, coords) in STAIRS) {
            val id = symbol.asRSCM(RSCMType.LOC)
            val loc = locRepo.findAll(coords).firstOrNull { it.id == id }
            if (loc == null) {
                missing += "$symbol@$coords"
                continue
            }
            locRepo.del(loc, duration = Int.MAX_VALUE)
        }
        if (missing.isNotEmpty()) {
            logger.warn { "Skilling zone: stairs not found: ${missing.joinToString()}" }
        }
    }

    private companion object {
        private val ENTRANCE = CoordGrid(3264, 6070, 0)

        private val STAIRS =
            listOf(
                "loc.prif_crystal_staircase_a" to CoordGrid(3260, 6086, 0),
                "loc.prif_crystal_staircase_b" to CoordGrid(3261, 6086, 0),
                "loc.prif_crystal_staircase_c" to CoordGrid(3267, 6086, 0),
                "loc.prif_crystal_staircase_d" to CoordGrid(3260, 6080, 0),
                "loc.prif_crystal_staircase_e" to CoordGrid(3261, 6083, 0),
                "loc.prif_crystal_staircase_f" to CoordGrid(3264, 6083, 0),
                "loc.prif_crystal_staircase_g" to CoordGrid(3261, 6080, 0),
                "loc.prif_crystal_staircase_h" to CoordGrid(3264, 6080, 0),
                "loc.prif_crystal_staircase_i" to CoordGrid(3267, 6080, 0),
                "loc.prif_crystal_staircase_j" to CoordGrid(3260, 6079, 0),
                "loc.prif_crystal_staircase_k" to CoordGrid(3263, 6079, 0),
                "loc.prif_crystal_staircase_l" to CoordGrid(3265, 6079, 0),
                "loc.prif_crystal_staircase_m" to CoordGrid(3262, 6081, 2),
                "loc.prif_crystal_staircase_top_arches" to CoordGrid(3266, 6082, 2),
            )
    }
}
