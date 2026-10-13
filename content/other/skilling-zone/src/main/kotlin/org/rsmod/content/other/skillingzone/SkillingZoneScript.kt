package org.rsmod.content.other.skillingzone

import jakarta.inject.Inject
import org.rsmod.api.player.hook.TeleportType
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.script.onCommand
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class SkillingZoneScript
@Inject
constructor(private val protectedAccess: ProtectedAccessLauncher) : PluginScript() {
    override fun ScriptContext.startup() {
        onCommand("skilling") {
            desc = "Teleport to the Prifddinas skilling zone"
            cheat { protectedAccess.launch(player) { telejump(ENTRANCE, TeleportType.Exempt) } }
        }
    }

    private companion object {
        private val ENTRANCE = CoordGrid(3264, 6070, 0)
    }
}
