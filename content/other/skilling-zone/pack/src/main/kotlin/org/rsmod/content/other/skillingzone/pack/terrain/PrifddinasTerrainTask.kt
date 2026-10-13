package org.rsmod.content.other.skillingzone.pack.terrain

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.cache.MAPS
import dev.openrune.cache.tools.tasks.CacheTask
import dev.openrune.filesystem.Cache

/**
 * Applies [PrifddinasTerrain] and then [PrifddinasLocs] to each affected map square. The cache
 * tool runs pack tasks on the live (client) pass and again on the server pass, and both caches
 * store a map square as group `x shl 8 or z` with the terrain in file 0 and the locs in file 1, so
 * one task keeps them in agreement.
 */
class PrifddinasTerrainTask : CacheTask() {
    private val logger = InlineLogger()

    override fun init(cache: Cache) {
        var edited = 0
        var removedLocs = 0
        for (square in PrifddinasTerrain.MAP_SQUARES) {
            val group = (square.x shl 8) or square.z
            val terrain = cache.data(MAPS, group, TERRAIN_FILE, null)
            if (terrain == null) {
                logger.warn { "Prifddinas terrain: map square ${square.x}_${square.z} is missing." }
                continue
            }
            val updated = PrifddinasTerrain.edit(square, terrain)
            if (!updated.contentEquals(terrain)) {
                cache.write(MAPS, group, TERRAIN_FILE, updated, null)
                edited++
            }

            val locs = cache.data(MAPS, group, LOC_FILE, null) ?: continue
            val cleared = PrifddinasLocs.edit(square, updated, locs)
            if (cleared.removed > 0) {
                cache.write(MAPS, group, LOC_FILE, cleared.data, null)
                removedLocs += cleared.removed
            }
        }
        logger.info { "Prifddinas terrain: rewrote $edited map square(s)." }
        logger.info {
            "Prifddinas locs: removed $removedLocs of ${PrifddinasLocs.entryCount} listed loc(s)."
        }
    }

    private companion object {
        private const val TERRAIN_FILE = 0
        private const val LOC_FILE = 1
    }
}
