package org.rsmod.content.other.skillingzone.zones

import jakarta.inject.Inject
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onGameStartup
import org.rsmod.game.MapClock
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillingZones
@Inject
constructor(
    private val locRepo: LocRepository,
    private val npcRepo: NpcRepository,
    private val mapClock: MapClock,
) : PluginScript() {
    private val regrowing = ZONES.flatMap { it.spawns }.filter { it.regrow }

    override fun ScriptContext.startup() {
        onGameStartup { ZONES.forEach(::spawn) }
        onEvent<GameLifecycle.LateCycle> { regrow() }
    }

    private fun spawn(zone: Zone) {
        for (spawn in zone.spawns) {
            locRepo.add(spawn.toLocInfo(), Int.MAX_VALUE)
        }
        for (spawn in zone.npcs) {
            npcRepo.add(Npc(spawn.npc, spawn.coords), Int.MAX_VALUE)
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

        private val ZONES = listOf(WoodcuttingZone, FishingZone, CookingZone)
    }
}
