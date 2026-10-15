package org.rsmod.content.other.skillingzone.pack.terrain

import java.io.ByteArrayOutputStream
import org.rsmod.map.square.MapSquareGrid
import org.rsmod.map.square.MapSquareKey

/**
 * Strips the raised floors (walkways, clan tower platforms, the Amlodd house and its bridge) that
 * are left behind once their locs are cleared, so they no longer render floating over the city.
 *
 * Every tile inside the city wall, but outside the central tower and the kept buildings, loses its
 * overlay and underlay on planes 1-3 and its LINK_BELOW flag on plane 1. The tower's four diagonal
 * walkway arms, beyond the pillars where they join it, count as outside. On ground level the
 * footprints of cleared houses lose the BLOCKED flag their walls left behind, the old paving and
 * floor overlays are replaced by grass everywhere except the path network, which is paved with the
 * path overlay, and each reed pond copy takes the ground tiles of the south-east reed pond, water
 * and all. The wall and everything outside it are left untouched. The rewrite is idempotent, so it
 * can run on every build against a cache it has already edited.
 */
object PrifddinasTerrain {

    private const val LEVEL_COUNT = 4
    private const val BRIDGE_FLAG_LEVEL = 1
    private const val BLOCKED = 0x1
    private const val LINK_BELOW = 0x2
    private const val TILES_PER_LEVEL = MapSquareGrid.LENGTH * MapSquareGrid.LENGTH
    private const val PATH_OVERLAY = 106
    private const val GRASS_UNDERLAY = 48
    private const val OVERLAY_OPCODE_SHAPE_0 = 2

    private const val OPCODE_END = 0
    private const val OPCODE_HEIGHT = 1
    private const val OPCODE_OVERLAY_LAST = 49
    private const val OPCODE_SETTINGS_LAST = 81
    private const val SETTINGS_OFFSET = 49
    private const val UNDERLAY_OFFSET = 81

    private const val CITY_WEST_X = 3210
    private const val CITY_EAST_X = 3317
    private const val CITY_SOUTH_Z = 6025
    private const val CITY_NORTH_Z = 6134
    private const val CITY_SOUTH_WEST_MIN_X_PLUS_Z = 9253
    private const val CITY_NORTH_EAST_MAX_X_PLUS_Z = 9433
    private const val CITY_SOUTH_EAST_MIN_Z_MINUS_X = 2726
    private const val CITY_NORTH_WEST_MAX_Z_MINUS_X = 2906

    private val CENTRAL_TOWER = Box(3248, 6067, 3280, 6100)

    private const val NORTH_WEST_ARM_MIN_Z_MINUS_X = 2841
    private const val NORTH_EAST_ARM_MIN_X_PLUS_Z = 9368
    private const val SOUTH_WEST_ARM_MAX_X_PLUS_Z = 9324
    private const val SOUTH_EAST_ARM_MAX_Z_MINUS_X = 2797

    private val KEPT_BUILDINGS =
        listOf(
            Box(3215, 6085, 3222, 6092),
            Box(3253, 6037, 3259, 6042),
            Box(3218, 6111, 3225, 6118),
            Box(3306, 6052, 3311, 6058),
        )

    private val KEPT_AREAS = listOf(CENTRAL_TOWER) + KEPT_BUILDINGS

    private val CLEARED_HOUSE_FOOTPRINTS =
        listOf(
            Box(3228, 6085, 3235, 6092),
            Box(3266, 6123, 3272, 6128),
            Box(3268, 6118, 3270, 6119),
        )

    private val STRIPPED_OVERLAYS = setOf(106, 81, 61, 22, 42)
    private val RECOLOURED_UNDERLAYS = setOf(67, 50, 115, 19)

    private val PATH_ZONES =
        listOf(
            Box(3263, 6037, 3265, 6066),
            Box(3253, 6044, 3263, 6045),
            Box(3247, 6082, 3248, 6090),
            Box(3247, 6082, 3250, 6083),
            Box(3281, 6082, 3298, 6083),
            Box(3297, 6052, 3298, 6081),
            Box(3297, 6052, 3303, 6053),
            Box(3263, 6101, 3264, 6117),
            Box(3263, 6116, 3272, 6117),
            Box(3271, 6116, 3272, 6118),
            Box(3253, 6044, 3254, 6047),
            Box(3271, 6116, 3274, 6118),
        )

    private val TOWER_RING =
        listOf(
            Box(3248, 6067, 3249, 6100),
            Box(3279, 6067, 3280, 6100),
            Box(3248, 6067, 3280, 6068),
            Box(3248, 6099, 3280, 6100),
        )

    private val REED_POND = Box(3299, 6039, 3303, 6043)
    private val REED_POND_COPY_CORNERS = listOf(3304 to 6045)

    val MAP_SQUARES: List<MapSquareKey> =
        (squareOf(CITY_WEST_X)..squareOf(CITY_EAST_X)).flatMap { x ->
            (squareOf(CITY_SOUTH_Z)..squareOf(CITY_NORTH_Z)).map { z -> MapSquareKey(x, z) }
        }

    fun isCleared(x: Int, z: Int): Boolean =
        isInsideCity(x, z) && (KEPT_AREAS.none { it.contains(x, z) } || isTowerWalkwayArm(x, z))

    private fun isClearedHouse(x: Int, z: Int): Boolean =
        CLEARED_HOUSE_FOOTPRINTS.any { it.contains(x, z) }

    private fun isRegrassed(x: Int, z: Int): Boolean =
        isInsideCity(x, z) && KEPT_AREAS.none { it.contains(x, z) }

    private fun isPath(x: Int, z: Int): Boolean = PATH_ZONES.any { it.contains(x, z) }

    private fun isTowerRing(x: Int, z: Int): Boolean = TOWER_RING.any { it.contains(x, z) }

    private fun reedPondSource(x: Int, z: Int): Pair<Int, Int>? {
        for ((cornerX, cornerZ) in REED_POND_COPY_CORNERS) {
            val sourceX = REED_POND.minX + x - cornerX
            val sourceZ = REED_POND.minZ + z - cornerZ
            if (x >= cornerX && z >= cornerZ && REED_POND.contains(sourceX, sourceZ)) {
                return sourceX to sourceZ
            }
        }
        return null
    }

    private fun isTowerWalkwayArm(x: Int, z: Int): Boolean =
        CENTRAL_TOWER.contains(x, z) &&
            (z - x >= NORTH_WEST_ARM_MIN_Z_MINUS_X ||
                x + z >= NORTH_EAST_ARM_MIN_X_PLUS_Z ||
                x + z <= SOUTH_WEST_ARM_MAX_X_PLUS_Z ||
                z - x <= SOUTH_EAST_ARM_MAX_Z_MINUS_X)

    private fun isInsideCity(x: Int, z: Int): Boolean =
        x in CITY_WEST_X..CITY_EAST_X &&
            z in CITY_SOUTH_Z..CITY_NORTH_Z &&
            x + z >= CITY_SOUTH_WEST_MIN_X_PLUS_Z &&
            x + z <= CITY_NORTH_EAST_MAX_X_PLUS_Z &&
            z - x >= CITY_SOUTH_EAST_MIN_Z_MINUS_X &&
            z - x <= CITY_NORTH_WEST_MAX_Z_MINUS_X

    /**
     * Rewrites a map square's terrain file. Each tile is a run of `u16` opcodes ending in `0`, or
     * in `1` plus a height byte: `2..49` carry a `u16` overlay id, `50..81` the tile settings and
     * `82+` the underlay. Opcodes are copied through in their original order, so every tile outside
     * the edit area comes out byte-for-byte identical.
     */
    fun edit(square: MapSquareKey, data: ByteArray): ByteArray {
        val baseX = square.x * MapSquareGrid.LENGTH
        val baseZ = square.z * MapSquareGrid.LENGTH
        val out = ByteArrayOutputStream(data.size)
        val tileStarts by lazy { tileStarts(data) }
        var pos = 0
        for (level in 0 until LEVEL_COUNT) {
            for (x in 0 until MapSquareGrid.LENGTH) {
                for (z in 0 until MapSquareGrid.LENGTH) {
                    val pondSource = if (level == 0) reedPondSource(baseX + x, baseZ + z) else null
                    if (pondSource != null) {
                        val (sourceX, sourceZ) = pondSource
                        check(sourceX - baseX in 0 until MapSquareGrid.LENGTH) { "Pond $pondSource" }
                        check(sourceZ - baseZ in 0 until MapSquareGrid.LENGTH) { "Pond $pondSource" }
                        val source = tileIndex(0, sourceX - baseX, sourceZ - baseZ)
                        val start = tileStarts[source]
                        out.write(data, start, tileStarts[source + 1] - start)
                        pos = tileStarts[tileIndex(level, x, z) + 1]
                        continue
                    }
                    val cleared = isCleared(baseX + x, baseZ + z)
                    val clearFloor = cleared && level > 0
                    val unlink = cleared && level == BRIDGE_FLAG_LEVEL
                    val unblock = level == 0 && isClearedHouse(baseX + x, baseZ + z)
                    if (level == 0 && isRegrassed(baseX + x, baseZ + z)) {
                        pos = rewriteGround(data, pos, out, isPath(baseX + x, baseZ + z), unblock)
                        continue
                    }
                    if (level == 0 && isTowerRing(baseX + x, baseZ + z)) {
                        pos = rewriteGround(data, pos, out, true, unblock = false, ring = true)
                        continue
                    }
                    while (true) {
                        val opcode = data.u16(pos)
                        pos += 2
                        when {
                            opcode == OPCODE_END -> {
                                out.writeShort(opcode)
                                break
                            }
                            opcode == OPCODE_HEIGHT -> {
                                out.writeShort(opcode)
                                out.write(data[pos++].toInt())
                                break
                            }
                            opcode <= OPCODE_OVERLAY_LAST -> {
                                if (!clearFloor) {
                                    out.writeShort(opcode)
                                    out.write(data, pos, 2)
                                }
                                pos += 2
                            }
                            opcode <= OPCODE_SETTINGS_LAST -> {
                                val settings = opcode - SETTINGS_OFFSET
                                var kept = if (unlink) settings and LINK_BELOW.inv() else settings
                                if (unblock) {
                                    kept = kept and BLOCKED.inv()
                                }
                                if (kept != 0) {
                                    out.writeShort(kept + SETTINGS_OFFSET)
                                }
                            }
                            !clearFloor -> out.writeShort(opcode)
                        }
                    }
                }
            }
        }
        out.write(data, pos, data.size - pos)
        return out.toByteArray()
    }

    private fun rewriteGround(
        data: ByteArray,
        start: Int,
        out: ByteArrayOutputStream,
        path: Boolean,
        unblock: Boolean,
        ring: Boolean = false,
    ): Int {
        var pos = start
        var overlay = -1
        var overlayOpcode = OVERLAY_OPCODE_SHAPE_0
        var settings = 0
        var underlay = -1
        var terminator = OPCODE_END
        var height = 0
        while (true) {
            val opcode = data.u16(pos)
            pos += 2
            if (opcode == OPCODE_END) {
                break
            }
            if (opcode == OPCODE_HEIGHT) {
                terminator = OPCODE_HEIGHT
                height = data[pos++].toInt()
                break
            }
            when {
                opcode <= OPCODE_OVERLAY_LAST -> {
                    overlayOpcode = opcode
                    overlay = data.u16(pos)
                    pos += 2
                }
                opcode <= OPCODE_SETTINGS_LAST -> settings = opcode - SETTINGS_OFFSET
                else -> underlay = opcode - UNDERLAY_OFFSET
            }
        }
        if (ring && overlay != -1 && overlay != PATH_OVERLAY) {
            out.write(data, start, pos - start)
            return pos
        }
        if (!path) {
            if (overlay in STRIPPED_OVERLAYS) {
                overlay = -1
                underlay = GRASS_UNDERLAY
            } else if (overlay == -1 && underlay in RECOLOURED_UNDERLAYS) {
                underlay = GRASS_UNDERLAY
            }
        } else if (overlay == -1) {
            overlay = PATH_OVERLAY
            overlayOpcode = OVERLAY_OPCODE_SHAPE_0
            if (underlay == -1) {
                underlay = GRASS_UNDERLAY
            }
        } else if (overlay == PATH_OVERLAY) {
            overlayOpcode = OVERLAY_OPCODE_SHAPE_0
        }
        if (unblock) {
            settings = settings and BLOCKED.inv()
        }
        if (overlay != -1) {
            out.writeShort(overlayOpcode)
            out.writeShort(overlay)
        }
        if (settings != 0) {
            out.writeShort(settings + SETTINGS_OFFSET)
        }
        if (underlay != -1) {
            out.writeShort(underlay + UNDERLAY_OFFSET)
        }
        out.writeShort(terminator)
        if (terminator == OPCODE_HEIGHT) {
            out.write(height)
        }
        return pos
    }

    fun linkBelowTiles(data: ByteArray): BooleanArray {
        val linked = BooleanArray(MapSquareGrid.LENGTH * MapSquareGrid.LENGTH)
        var pos = 0
        for (level in 0 until LEVEL_COUNT) {
            for (x in 0 until MapSquareGrid.LENGTH) {
                for (z in 0 until MapSquareGrid.LENGTH) {
                    while (true) {
                        val opcode = data.u16(pos)
                        pos += 2
                        if (opcode == OPCODE_END) {
                            break
                        }
                        if (opcode == OPCODE_HEIGHT) {
                            pos++
                            break
                        }
                        if (opcode <= OPCODE_OVERLAY_LAST) {
                            pos += 2
                        } else if (opcode <= OPCODE_SETTINGS_LAST && level == BRIDGE_FLAG_LEVEL) {
                            val settings = opcode - SETTINGS_OFFSET
                            if (settings and LINK_BELOW != 0) {
                                linked[x * MapSquareGrid.LENGTH + z] = true
                            }
                        }
                    }
                }
            }
        }
        return linked
    }

    private fun tileStarts(data: ByteArray): IntArray {
        val starts = IntArray(LEVEL_COUNT * TILES_PER_LEVEL + 1)
        var pos = 0
        for (tile in 0 until LEVEL_COUNT * TILES_PER_LEVEL) {
            starts[tile] = pos
            while (true) {
                val opcode = data.u16(pos)
                pos += 2
                if (opcode == OPCODE_END) {
                    break
                }
                if (opcode == OPCODE_HEIGHT) {
                    pos++
                    break
                }
                if (opcode <= OPCODE_OVERLAY_LAST) {
                    pos += 2
                }
            }
        }
        starts[LEVEL_COUNT * TILES_PER_LEVEL] = pos
        return starts
    }

    private fun tileIndex(level: Int, x: Int, z: Int): Int =
        level * TILES_PER_LEVEL + x * MapSquareGrid.LENGTH + z

    private fun squareOf(coord: Int): Int = coord / MapSquareGrid.LENGTH

    private fun ByteArray.u16(pos: Int): Int =
        ((this[pos].toInt() and 0xFF) shl 8) or (this[pos + 1].toInt() and 0xFF)

    private fun ByteArrayOutputStream.writeShort(value: Int) {
        write(value shr 8)
        write(value)
    }

    private data class Box(val minX: Int, val minZ: Int, val maxX: Int, val maxZ: Int) {
        fun contains(x: Int, z: Int): Boolean = x in minX..maxX && z in minZ..maxZ
    }
}
