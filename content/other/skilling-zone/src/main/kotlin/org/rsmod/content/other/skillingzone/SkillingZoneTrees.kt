package org.rsmod.content.other.skillingzone

import com.github.michaelbull.logging.InlineLogger
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import org.rsmod.api.game.process.GameLifecycle
import org.rsmod.api.repo.loc.LocRepository
import org.rsmod.api.script.onEvent
import org.rsmod.api.script.onGameStartup
import org.rsmod.game.MapClock
import org.rsmod.game.loc.LocAngle
import org.rsmod.game.loc.LocEntity
import org.rsmod.game.loc.LocInfo
import org.rsmod.game.loc.LocShape
import org.rsmod.map.CoordGrid
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext
import org.rsmod.routefinder.loc.LocLayerConstants

class SkillingZoneTrees
@Inject
constructor(private val locRepo: LocRepository, private val mapClock: MapClock) :
    PluginScript() {
    private val logger = InlineLogger()

    override fun ScriptContext.startup() {
        onGameStartup {
            clearScenery()
            plantTrees()
        }
        onEvent<GameLifecycle.LateCycle> { regrowTrees() }
    }

    private fun clearScenery() {
        val missing = mutableListOf<ClearedScenery>()
        for (scenery in CLEARED_SCENERY) {
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
                "Skilling zone: scenery not found at startup: " +
                    missing.joinToString { "${it.loc}@${it.coords}" }
            }
        }
    }

    private fun plantTrees() {
        for (tree in TREES) {
            locRepo.add(tree.toLoc(), Int.MAX_VALUE)
        }
    }

    private fun regrowTrees() {
        if (mapClock % REGROW_INTERVAL != 0) {
            return
        }
        for (tree in TREES) {
            val loc = tree.toLoc()
            if (locRepo.findAll(loc.coords).none { it.layer == loc.layer }) {
                locRepo.add(loc, Int.MAX_VALUE)
            }
        }
    }

    private fun TreeSpawn.toLoc(): LocInfo {
        val shape = LocShape.CentrepieceStraight
        val entity = LocEntity(loc.asRSCM(RSCMType.LOC), shape.id, angle.id)
        return LocInfo(LocLayerConstants.of(shape.id), coords, entity)
    }

    private data class TreeSpawn(val loc: String, val coords: CoordGrid, val angle: LocAngle)

    private data class ClearedScenery(val loc: String, val coords: CoordGrid)

    private companion object {
        private const val REGROW_INTERVAL = 5

        private const val TREE = "loc.tree"
        private const val OAK = "loc.oaktree"
        private const val WILLOW = "loc.willowtree"
        private const val MAPLE = "loc.mapletree"
        private const val YEW = "loc.yewtree"
        private const val MAGIC = "loc.magictree"
        private const val TEAK = "loc.teaktree"
        private const val MAHOGANY = "loc.mahoganytree"

        private const val PRIF_TREE_SMALL = "loc.prif_tree_normal_1"
        private const val PRIF_TREE_LARGE = "loc.prif_tree_normal_2"
        private const val BUSH = "loc.plants_update_small_bush_grassland"
        private const val ROCK_1 = "loc.regicide_rock1"
        private const val ROCK_2 = "loc.regicide_rock2"
        private const val ROCK_3 = "loc.regicide_rock3"

        private fun tree(loc: String, x: Int, z: Int, angle: LocAngle) =
            TreeSpawn(loc, CoordGrid(x, z, 0), angle)

        private fun scenery(loc: String, x: Int, z: Int) = ClearedScenery(loc, CoordGrid(x, z, 0))

        private val TREES =
            listOf(
                tree(TREE, 3236, 6086, LocAngle.North),
                tree(TREE, 3247, 6085, LocAngle.West),
                tree(TREE, 3250, 6087, LocAngle.East),
                tree(TREE, 3251, 6090, LocAngle.South),
                tree(OAK, 3239, 6085, LocAngle.South),
                tree(OAK, 3247, 6090, LocAngle.North),
                tree(OAK, 3243, 6091, LocAngle.East),
                tree(WILLOW, 3251, 6093, LocAngle.West),
                tree(WILLOW, 3251, 6097, LocAngle.East),
                tree(WILLOW, 3237, 6091, LocAngle.North),
                tree(MAPLE, 3245, 6095, LocAngle.South),
                tree(MAPLE, 3248, 6094, LocAngle.North),
                tree(YEW, 3237, 6096, LocAngle.East),
                tree(YEW, 3254, 6094, LocAngle.West),
                tree(YEW, 3248, 6101, LocAngle.South),
                tree(MAGIC, 3238, 6102, LocAngle.North),
                tree(MAGIC, 3252, 6102, LocAngle.South),
                tree(MAHOGANY, 3241, 6097, LocAngle.West),
                tree(TEAK, 3238, 6100, LocAngle.East),
            )

        private val CLEARED_SCENERY =
            listOf(
                scenery(ROCK_1, 3236, 6085),
                scenery(ROCK_1, 3249, 6101),
                scenery(ROCK_2, 3243, 6096),
                scenery(ROCK_3, 3242, 6097),
                scenery(ROCK_3, 3245, 6096),
                scenery(BUSH, 3248, 6094),
                scenery(BUSH, 3251, 6087),
                scenery(BUSH, 3252, 6090),
                scenery(BUSH, 3252, 6098),
                scenery(PRIF_TREE_SMALL, 3236, 6087),
                scenery(PRIF_TREE_SMALL, 3237, 6095),
                scenery(PRIF_TREE_SMALL, 3237, 6103),
                scenery(PRIF_TREE_SMALL, 3239, 6101),
                scenery(PRIF_TREE_SMALL, 3241, 6087),
                scenery(PRIF_TREE_SMALL, 3243, 6093),
                scenery(PRIF_TREE_SMALL, 3248, 6092),
                scenery(PRIF_TREE_SMALL, 3249, 6094),
                scenery(PRIF_TREE_SMALL, 3249, 6103),
                scenery(PRIF_TREE_SMALL, 3250, 6089),
                scenery(PRIF_TREE_SMALL, 3251, 6092),
                scenery(PRIF_TREE_SMALL, 3251, 6094),
                scenery(PRIF_TREE_SMALL, 3251, 6096),
                scenery(PRIF_TREE_SMALL, 3253, 6094),
                scenery(PRIF_TREE_SMALL, 3255, 6094),
                scenery(PRIF_TREE_LARGE, 3248, 6085),
                scenery(PRIF_TREE_LARGE, 3251, 6102),
                scenery(PRIF_TREE_LARGE, 3254, 6096),
            )
    }
}
