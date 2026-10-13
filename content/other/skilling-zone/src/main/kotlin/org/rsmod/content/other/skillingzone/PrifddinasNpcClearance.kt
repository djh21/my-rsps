package org.rsmod.content.other.skillingzone

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import org.rsmod.api.repo.npc.NpcRepository
import org.rsmod.api.script.onGameStartup
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.NpcList
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class PrifddinasNpcClearance
@Inject
constructor(private val npcList: NpcList, private val npcRepo: NpcRepository) : PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        onGameStartup { clearNpcs() }
    }

    private fun clearNpcs() {
        val npcs = npcList.filter { it.inPrifddinas() }
        for (npc in npcs) {
            npcRepo.del(npc, Int.MAX_VALUE)
        }
        logger.info { "Prifddinas: removed ${npcs.size} npc(s)." }
    }

    private fun Npc.inPrifddinas(): Boolean =
        coords.x in MIN_X..MAX_X && coords.z in MIN_Z..MAX_Z

    private companion object {
        private const val MIN_X = 3200
        private const val MAX_X = 3327
        private const val MIN_Z = 5952
        private const val MAX_Z = 6143
    }
}
