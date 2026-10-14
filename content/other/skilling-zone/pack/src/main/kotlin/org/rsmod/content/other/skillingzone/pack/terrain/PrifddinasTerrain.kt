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
 * footprints of cleared houses lose the BLOCKED flag their walls left behind, the grassed skill
 * zones have their path overlay stripped back to the grass beneath, and each reed pond copy takes
 * the ground tiles of the south-east reed pond, water and all. The wall and everything outside it
 * are left untouched. The rewrite is idempotent, so it can run on every build against a cache it
 * has already edited.
 */
object PrifddinasTerrain {

    private const val LEVEL_COUNT = 4
    private const val BRIDGE_FLAG_LEVEL = 1
    private const val BLOCKED = 0x1
    private const val LINK_BELOW = 0x2
    private const val TILES_PER_LEVEL = MapSquareGrid.LENGTH * MapSquareGrid.LENGTH

    private const val OPCODE_END = 0
    private const val OPCODE_HEIGHT = 1
    private const val OPCODE_OVERLAY_LAST = 49
    private const val OPCODE_SETTINGS_LAST = 81
    private const val SETTINGS_OFFSET = 49
    private const val PATH_OVERLAY = 106

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
            Box(3266, 6123, 3272, 6128),
        )

    private val KEPT_AREAS = listOf(CENTRAL_TOWER) + KEPT_BUILDINGS

    private val CLEARED_HOUSE_FOOTPRINTS = listOf(Box(3228, 6085, 3235, 6092))

    private val GRASSED_ZONES = listOf(Box(3293, 6036, 3310, 6051))

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

    private fun isGrassedZone(x: Int, z: Int): Boolean = GRASSED_ZONES.any { it.contains(x, z) }

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
                    val grass = level == 0 && isGrassedZone(baseX + x, baseZ + z)
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
                                val path = grass && data.u16(pos) == PATH_OVERLAY
                                if (!clearFloor && !path) {
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
