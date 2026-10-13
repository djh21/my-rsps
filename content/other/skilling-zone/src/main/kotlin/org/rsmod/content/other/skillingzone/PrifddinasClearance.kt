package org.rsmod.content.other.skillingzone

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onGameStartup
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class PrifddinasClearance @Inject constructor(private val locRepo: LocRepository) :
    PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        onGameStartup { clearScenery() }
    }

    private fun clearScenery() {
        val cleared = loadEntries()
        val missing = mutableListOf<ClearedLoc>()
        for (entry in cleared) {
            val type = entry.loc.asRSCM(RSCMType.LOC)
            val loc =
                locRepo.findAll(entry.coords).firstOrNull {
                    it.id == type && it.shapeId == entry.shape
                }
            if (loc == null) {
                missing += entry
                continue
            }
            locRepo.del(loc, duration = Int.MAX_VALUE)
        }

        logger.info { "Prifddinas: removed ${cleared.size - missing.size} loc(s)." }
        if (missing.isNotEmpty()) {
            logger.warn {
                "Prifddinas: scenery not found at startup: " +
                    missing.joinToString { "${it.loc}@${it.coords}" }
            }
        }
    }

    private fun loadEntries(): List<ClearedLoc> {
        val stream =
            PrifddinasClearance::class.java.classLoader.getResourceAsStream(RESOURCE)
                ?: error("Missing plugin resource: $RESOURCE")
        return stream.bufferedReader().useLines { lines ->
            lines
                .filter { it.isNotBlank() }
                .map { line ->
                    val (loc, x, z, level, shape) = line.split(' ')
                    ClearedLoc(loc, CoordGrid(x.toInt(), z.toInt(), level.toInt()), shape.toInt())
                }
                .toList()
        }
    }

    private data class ClearedLoc(val loc: String, val coords: CoordGrid, val shape: Int)

    private companion object {
        private const val RESOURCE = "prifddinas-clearance.txt"
    }
}
