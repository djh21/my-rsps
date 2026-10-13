package org.rsmod.content.other.skillingzone.pack

import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.pack.PluginPack
import org.rsmod.content.other.skillingzone.pack.terrain.PrifddinasTerrainTask

class SkillingZonePluginPack : PluginPack() {
    override fun extraTasks(): List<CacheTask> = listOf(PrifddinasTerrainTask())
}
