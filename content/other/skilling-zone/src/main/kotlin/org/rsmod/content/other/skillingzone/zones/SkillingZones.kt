package org.rsmod.content.other.skillingzone.zones

import jakarta.inject.Inject
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.script.onGameStartup
import org.rsmod.game.entity.Npc
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillingZones
@Inject
constructor(private val locRepo: LocRepository, private val npcRepo: NpcRepository) :
    PluginScript() {
    override fun ScriptContext.startup() {
        onGameStartup { ZONES.forEach(::spawn) }
    }

    private fun spawn(zone: Zone) {
        for (spawn in zone.spawns) {
            locRepo.add(spawn.toLocInfo(), Int.MAX_VALUE)
        }
        for (spawn in zone.npcs) {
            npcRepo.add(Npc(spawn.npc, spawn.coords), Int.MAX_VALUE)
        }
    }

    private companion object {
        private val ZONES = listOf(WoodcuttingZone, FishingZone, CookingZone)
    }
}
