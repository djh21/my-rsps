package org.rsmod.api.repo.loc

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.api.registry.controller.ControllerRegistry
import org.rsmod.api.registry.loc.LocRegistry
import org.rsmod.api.registry.loc.LocRegistryNormal
import org.rsmod.api.registry.loc.LocRegistryRegion
import org.rsmod.api.registry.npc.NpcRegistry
import org.rsmod.api.registry.region.RegionRegistry
import org.rsmod.api.registry.zone.ZonePlayerActivityBitSet
import org.rsmod.api.registry.zone.ZoneUpdateMap
import org.rsmod.events.EventBus
import org.rsmod.game.MapClock
import org.rsmod.game.entity.ControllerList
import org.rsmod.game.entity.NpcList
import org.rsmod.game.loc.LocEntity
import org.rsmod.game.loc.LocInfo
import org.rsmod.game.map.LocZoneStorage
import org.rsmod.game.region.RegionListLarge
import org.rsmod.game.region.RegionListSmall
import org.rsmod.game.region.RegionListWorldEntity
import org.rsmod.map.CoordGrid
import org.rsmod.routefinder.collision.CollisionFlagMap
import org.rsmod.routefinder.loc.LocLayerConstants

class LocRepositoryPermanentSpawnTest {
    @Test
    fun `a timed swap over a permanent spawn restores it in the same cycle it ends`() {
        val fixture = Fixture()
        fixture.locs.add(loc(ROCK), Int.MAX_VALUE)
        fixture.locs.add(loc(EMPTY_ROCK), RESPAWN_CYCLES)
        assertEquals(listOf(EMPTY_ROCK), fixture.idsAtTile())

        fixture.advance(RESPAWN_CYCLES - 1)
        assertEquals(listOf(EMPTY_ROCK), fixture.idsAtTile())

        fixture.advance(1)
        assertEquals(listOf(ROCK), fixture.idsAtTile())
    }

    @Test
    fun `a timed spawn with nothing permanent underneath despawns to an empty tile`() {
        val fixture = Fixture()
        fixture.locs.add(loc(EMPTY_ROCK), RESPAWN_CYCLES)

        fixture.advance(RESPAWN_CYCLES)
        assertEquals(emptyList<Int>(), fixture.idsAtTile())
    }

    @Test
    fun `a deleted permanent spawn is not brought back by a later timed spawn`() {
        val fixture = Fixture()
        fixture.locs.add(loc(ROCK), Int.MAX_VALUE)
        fixture.locs.del(loc(ROCK), Int.MAX_VALUE)
        fixture.locs.add(loc(EMPTY_ROCK), RESPAWN_CYCLES)

        fixture.advance(RESPAWN_CYCLES)
        assertEquals(emptyList<Int>(), fixture.idsAtTile())
    }

    @Test
    fun `a newer permanent spawn placed during the swap is kept`() {
        val fixture = Fixture()
        fixture.locs.add(loc(ROCK), Int.MAX_VALUE)
        fixture.locs.add(loc(EMPTY_ROCK), RESPAWN_CYCLES)
        fixture.locs.add(loc(OTHER_ROCK), Int.MAX_VALUE)

        fixture.advance(RESPAWN_CYCLES)
        assertEquals(listOf(OTHER_ROCK), fixture.idsAtTile())
    }

    @Test
    fun `the caller's despawn callback still runs after a restore`() {
        val fixture = Fixture()
        var despawned = false
        fixture.locs.add(loc(ROCK), Int.MAX_VALUE)
        fixture.locs.add(loc(EMPTY_ROCK), RESPAWN_CYCLES) { despawned = true }

        fixture.advance(RESPAWN_CYCLES)
        assertTrue(despawned)
        assertEquals(listOf(ROCK), fixture.idsAtTile())
    }

    private fun loc(id: Int): LocInfo =
        LocInfo(LocLayerConstants.of(SHAPE), TILE, LocEntity(id, SHAPE, ANGLE))

    private class Fixture {
        private val collision = CollisionFlagMap()
        private val clock = MapClock().apply { cycle = 100 }
        private val updates = ZoneUpdateMap()
        private val zones = LocZoneStorage()
        private val normal = LocRegistryNormal(updates, collision, zones)
        private val regions =
            RegionRegistry(
                RegionListSmall(),
                RegionListLarge(),
                RegionListWorldEntity(),
                normal,
                collision,
                zones,
                NpcRegistry(NpcList(), collision, EventBus()),
                ControllerRegistry(clock, ControllerList()),
                ZonePlayerActivityBitSet(),
            )
        private val registry =
            LocRegistry(zones, normal, LocRegistryRegion(updates, collision, zones, regions))
        val locs = LocRepository(clock, registry, regions)

        fun advance(cycles: Int) {
            repeat(cycles) {
                clock.tick()
                locs.processDurations()
            }
        }

        fun idsAtTile(): List<Int> = locs.findAll(TILE).map { it.id }.toList()
    }

    private companion object {
        private val TILE = CoordGrid(3200, 3200, 0)
        private const val SHAPE = 10
        private const val ANGLE = 0
        private const val ROCK = 11364
        private const val EMPTY_ROCK = 11390
        private const val OTHER_ROCK = 11366
        private const val RESPAWN_CYCLES = 9
    }
}
