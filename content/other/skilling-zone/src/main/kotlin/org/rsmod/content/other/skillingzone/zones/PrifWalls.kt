package org.rsmod.content.other.skillingzone.zones

import kotlin.math.abs
import kotlin.math.roundToInt
import org.rsmod.game.loc.LocAngle

enum class Side {
    South,
    North,
    West,
    East,
}

data class Entrance(val side: Side, val start: Int, val width: Int) {
    val tiles: IntRange
        get() = start until start + width
}

/**
 * Encloses the box from ([minX], [minZ]) to ([maxX], [maxZ]) in the two-tile-thick Prifddinas city
 * wall: a tall pillar on each corner, and each side filled with medium sections split by pillars at
 * roughly the city wall's spacing of one pillar per three sections. The [entrance] is left open and
 * flanked by pillars.
 */
internal fun prifWalls(minX: Int, minZ: Int, maxX: Int, maxZ: Int, entrance: Entrance): List<ZoneLoc> {
    val eastX = maxX - 1
    val northZ = maxZ - 1
    val corners = listOf(minX to minZ, eastX to minZ, minX to northZ, eastX to northZ)
    val walls = corners.map { (x, z) -> placed(PILLAR.loc, x, z, HORIZONTAL) }.toMutableList()

    val alongX = minX + WALL_THICKNESS..maxX - WALL_THICKNESS
    val alongZ = minZ + WALL_THICKNESS..maxZ - WALL_THICKNESS
    for (side in Side.entries) {
        val horizontal = side == Side.South || side == Side.North
        val fixed =
            when (side) {
                Side.South -> minZ
                Side.North -> northZ
                Side.West -> minX
                Side.East -> eastX
            }
        val gap = entrance.tiles.takeIf { entrance.side == side }
        for ((pos, piece) in side(if (horizontal) alongX else alongZ, gap)) {
            walls +=
                if (horizontal) {
                    placed(piece.loc, pos, fixed, HORIZONTAL)
                } else {
                    placed(piece.loc, fixed, pos, VERTICAL)
                }
        }
    }
    return walls
}

private fun side(tiles: IntRange, gap: IntRange?): List<Pair<Int, WallPiece>> {
    if (gap == null) {
        return layout(tiles.first, run(tiles.last - tiles.first + 1))
    }
    val before = gap.first - tiles.first
    val after = tiles.last - gap.last
    val beforePieces = if (before > 0) run(before - PILLAR.length) + PILLAR else emptyList()
    val afterPieces = if (after > 0) listOf(PILLAR) + run(after - PILLAR.length) else emptyList()
    return layout(tiles.first, beforePieces) + layout(gap.last + 1, afterPieces)
}

private fun layout(start: Int, pieces: List<WallPiece>): List<Pair<Int, WallPiece>> {
    var pos = start
    return pieces.map { piece -> (pos to piece).also { pos += piece.length } }
}

private fun run(length: Int): List<WallPiece> {
    require(length >= 0 && length != 1) { "A wall run can't be $length tile(s) long." }
    val target = length.toDouble() / PILLAR_SPACING
    val splits =
        (0..length / PILLAR.length).filter { (length - it * PILLAR.length) % SECTION.length == 0 }
    val balanced =
        splits.filter {
            val sectionCount = (length - it * PILLAR.length) / SECTION.length
            it == 0 || sectionCount == 0 || it < sectionCount
        }
    val pillars = balanced.ifEmpty { splits }.minBy { abs(it - target) }
    val sections = (length - pillars * PILLAR.length) / SECTION.length
    val pillarAfter = (1..pillars).map { (it * sections.toDouble() / (pillars + 1)).roundToInt() }

    val pieces = mutableListOf<WallPiece>()
    repeat(pillarAfter.count { it == 0 }) { pieces += PILLAR }
    for (i in 0 until sections) {
        pieces += if (i % 2 == 0) SECTION else SECTION_ALT
        repeat(pillarAfter.count { it == i + 1 }) { pieces += PILLAR }
    }
    return pieces
}

private data class WallPiece(val loc: String, val length: Int)

private const val WALL_THICKNESS = 2
private const val PILLAR_SPACING = 11

private val HORIZONTAL = LocAngle.East
private val VERTICAL = LocAngle.North

private val PILLAR = WallPiece("loc.prif_wall_tall_01", 2)
private val SECTION = WallPiece("loc.prif_wall_medium_02", 3)
private val SECTION_ALT = WallPiece("loc.prif_wall_medium_01", 3)
