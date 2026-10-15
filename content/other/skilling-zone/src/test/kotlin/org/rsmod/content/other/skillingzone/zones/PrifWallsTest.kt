package org.rsmod.content.other.skillingzone.zones

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.game.loc.LocAngle

class PrifWallsTest {
    @Test
    fun `every zone's walls close the ring except for the entrance`() {
        assertRingClosed(3223, 6085, 3246, 6106, Entrance(Side.East, start = 6087, width = 4))
        assertRingClosed(3293, 6036, 3310, 6051, Entrance(Side.North, start = 3300, width = 4))
        assertRingClosed(3299, 6060, 3311, 6079, Entrance(Side.West, start = 6064, width = 4))
        assertRingClosed(3266, 6030, 3288, 6046, Entrance(Side.West, start = 6037, width = 4))
        assertRingClosed(3266, 6052, 3288, 6063, Entrance(Side.West, start = 6056, width = 4))
        assertRingClosed(3267, 6119, 3293, 6132, Entrance(Side.South, start = 3271, width = 4))
        assertRingClosed(3220, 6029, 3252, 6049, Entrance(Side.East, start = 6044, width = 4))
        assertRingClosed(3284, 6084, 3313, 6118, Entrance(Side.South, start = 3296, width = 4))
    }

    @Test
    fun `a pillar-only stretch beside the entrance is still walled`() {
        val walls = prifWalls(3266, 6030, 3288, 6046, Entrance(Side.North, start = 3279, width = 4))
        val northRow = walls.filter { it.coords.z == 6045 }.map { it.coords.x to it.loc }
        assertTrue(3285 to PILLAR in northRow, "Expected a pillar at 3285 in $northRow")
    }

    @Test
    fun `walls close the ring for every buildable box and entrance`() {
        for (width in MIN_SIDE..MAX_SIDE) {
            for (length in MIN_SIDE..MAX_SIDE) {
                val maxX = BASE_X + width - 1
                val maxZ = BASE_Z + length - 1
                for (side in Side.entries) {
                    val horizontal = side == Side.South || side == Side.North
                    val first = (if (horizontal) BASE_X else BASE_Z) + 2
                    val last = (if (horizontal) maxX else maxZ) - 2
                    for (start in first..last - ENTRANCE_WIDTH + 1) {
                        val before = start - first
                        val after = last - (start + ENTRANCE_WIDTH - 1)
                        if (before in UNBUILDABLE_RUNS || after in UNBUILDABLE_RUNS) {
                            continue
                        }
                        val entrance = Entrance(side, start, ENTRANCE_WIDTH)
                        assertRingClosed(BASE_X, BASE_Z, maxX, maxZ, entrance)
                    }
                }
            }
        }
    }

    private fun assertRingClosed(minX: Int, minZ: Int, maxX: Int, maxZ: Int, entrance: Entrance) {
        val covered = mutableMapOf<Pair<Int, Int>, Int>()
        for (wall in prifWalls(minX, minZ, maxX, maxZ, entrance)) {
            for (tile in footprint(wall)) {
                covered.merge(tile, 1, Int::plus)
            }
        }
        val label = "box ($minX, $minZ)-($maxX, $maxZ) with $entrance"
        assertEquals(emptyMap<Pair<Int, Int>, Int>(), covered.filterValues { it > 1 }, "Overlap in $label")
        assertEquals(ring(minX, minZ, maxX, maxZ) - entranceTiles(minX, minZ, maxX, maxZ, entrance), covered.keys, label)
    }

    private fun footprint(wall: ZoneLoc): List<Pair<Int, Int>> {
        val (sizeX, sizeZ) =
            when {
                wall.loc == PILLAR -> 2 to 2
                wall.angle == LocAngle.East -> 3 to 2
                else -> 2 to 3
            }
        return (0 until sizeX).flatMap { dx ->
            (0 until sizeZ).map { dz -> wall.coords.x + dx to wall.coords.z + dz }
        }
    }

    private fun ring(minX: Int, minZ: Int, maxX: Int, maxZ: Int): Set<Pair<Int, Int>> =
        (minX..maxX)
            .flatMap { x -> (minZ..maxZ).map { z -> x to z } }
            .filter { (x, z) -> x <= minX + 1 || x >= maxX - 1 || z <= minZ + 1 || z >= maxZ - 1 }
            .toSet()

    private fun entranceTiles(
        minX: Int,
        minZ: Int,
        maxX: Int,
        maxZ: Int,
        entrance: Entrance,
    ): Set<Pair<Int, Int>> {
        val depth =
            when (entrance.side) {
                Side.South -> minZ..minZ + 1
                Side.North -> maxZ - 1..maxZ
                Side.West -> minX..minX + 1
                Side.East -> maxX - 1..maxX
            }
        return entrance.tiles
            .flatMap { along -> depth.map { across -> along to across } }
            .map { (along, across) ->
                if (entrance.side == Side.South || entrance.side == Side.North) {
                    along to across
                } else {
                    across to along
                }
            }
            .toSet()
    }

    private companion object {
        private const val PILLAR = "loc.prif_wall_tall_01"
        private const val BASE_X = 3200
        private const val BASE_Z = 6000
        private const val MIN_SIDE = 12
        private const val MAX_SIDE = 35
        private const val ENTRANCE_WIDTH = 4
        private val UNBUILDABLE_RUNS = setOf(1, 3)
    }
}
