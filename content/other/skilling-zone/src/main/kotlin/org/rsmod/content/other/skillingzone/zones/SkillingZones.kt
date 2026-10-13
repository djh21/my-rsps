package org.rsmod.content.other.skillingzone.zones

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onGameStartup
import org.rsmod.game.MapClock
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillingZones
@Inject
constructor(private val locRepo: LocRepository, private val mapClock: MapClock) : PluginScript() {
    private val logger = InlineLogger()

    private val regrowing = ZONES.flatMap { it.spawns }.filter { it.regrow }

    override fun ScriptContext.startup() {
        onGameStartup {
            for (zone in ZONES) {
                clearScenery(zone)
                spawn(zone)
            }
        }
        onEvent<GameLifecycle.LateCycle> { regrow() }
    }

    private fun clearScenery(zone: Zone) {
        val missing = mutableListOf<ZoneScenery>()
        for (scenery in zone.cleared) {
            val type = scenery.loc.asRSCM(RSCMType.LOC)
            val loc = locRepo.findAll(scenery.coords).firstOrNull { it.id == type }
            if (loc == null) {
                missing += scenery
                continue
            }
            locRepo.del(loc, duration = Int.MAX_VALUE)
        }

        if (missing.isNotEmpty()) {
            logger.warn {
                "Skilling zone ${zone.name}: scenery not found at startup: " +
                    missing.joinToString { "${it.loc}@${it.coords}" }
            }
        }
    }

    private fun spawn(zone: Zone) {
        for (spawn in zone.spawns) {
            locRepo.add(spawn.toLocInfo(), Int.MAX_VALUE)
        }
    }

    private fun regrow() {
        if (mapClock % REGROW_INTERVAL != 0) {
            return
        }
        for (spawn in regrowing) {
            val loc = spawn.toLocInfo()
            if (locRepo.findAll(loc.coords).none { it.layer == loc.layer }) {
                locRepo.add(loc, Int.MAX_VALUE)
            }
        }
    }

    private companion object {
        private const val REGROW_INTERVAL = 5

        private val ZONES = emptyList<Zone>()
    }
}
