package org.rsmod.content.other.skillingzone.pack.terrain

import dev.openrune.map.loc.MapLocDefinition
import dev.openrune.map.loc.MapLocListDecoder
import dev.openrune.map.util.InlineByteBuf
import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import it.unimi.dsi.fastutil.longs.LongArrayList
import java.io.ByteArrayOutputStream
import org.rsmod.map.square.MapSquareGrid
import org.rsmod.map.square.MapSquareKey

/**
 * Deletes the Prifddinas scenery listed in `prifddinas-clearance.txt` from the map squares'
 * loc files, so clients drawing the expanded scene straight from the cache never see it either.
 *
 * Entries are `symbol x z level shape`, with `level` as the server stores it: one below the
 * authored plane on tiles whose plane-1 settings carry LINK_BELOW. The terrain must already be
 * edited by [PrifddinasTerrain] so the bridge flags match the ones the server decodes.
 */
object PrifddinasLocs {

    private const val RESOURCE = "prifddinas-clearance.txt"
    private const val SMART_BYTE_MAX = 127
    private const val SMART_SHORT_FLAG = 0x8000
    private const val INCR_SMART_STEP = 0x7FFF

    private val CLEARED: Set<ClearedLoc> by lazy(::loadEntries)

    val entryCount: Int
        get() = CLEARED.size

    fun edit(square: MapSquareKey, terrain: ByteArray, locs: ByteArray): Result {
        val linkBelow = PrifddinasTerrain.linkBelowTiles(terrain)
        val baseX = square.x * MapSquareGrid.LENGTH
        val baseZ = square.z * MapSquareGrid.LENGTH
        val decoded = MapLocListDecoder.decode(InlineByteBuf(locs)).spawns
        val kept = LongArrayList(decoded.size)
        for (packed in decoded.longIterator()) {
            val loc = MapLocDefinition(packed)
            val shift = if (linkBelow[loc.localX * MapSquareGrid.LENGTH + loc.localZ]) 1 else 0
            val key =
                ClearedLoc(
                    loc.id,
                    baseX + loc.localX,
                    baseZ + loc.localZ,
                    loc.level - shift,
                    loc.shape,
                )
            if (key !in CLEARED) {
                kept += packed
            }
        }
        val removed = decoded.size - kept.size
        return if (removed == 0) Result(locs, 0) else Result(encode(kept), removed)
    }

    private fun encode(locs: LongArrayList): ByteArray {
        val out = ByteArrayOutputStream()
        var lastId = -1
        var index = 0
        while (index < locs.size) {
            val id = MapLocDefinition(locs.getLong(index)).id
            out.writeIncrShortSmart(id - lastId)
            lastId = id
            var lastCoord = 0
            while (index < locs.size) {
                val loc = MapLocDefinition(locs.getLong(index))
                if (loc.id != id) {
                    break
                }
                val coord = loc.packedCoord()
                out.writeShortSmart(coord - lastCoord + 1)
                out.write(loc.packedAttributes())
                lastCoord = coord
                index++
            }
            out.writeShortSmart(0)
        }
        out.writeShortSmart(0)
        return out.toByteArray()
    }

    private fun loadEntries(): Set<ClearedLoc> {
        val stream =
            PrifddinasLocs::class.java.classLoader.getResourceAsStream(RESOURCE)
                ?: error("Missing pack resource: $RESOURCE")
        return stream.bufferedReader().useLines { lines ->
            lines
                .filter { it.isNotBlank() }
                .map { line ->
                    val (loc, x, z, level, shape) = line.split(' ')
                    ClearedLoc(
                        loc.asRSCM(RSCMType.LOC),
                        x.toInt(),
                        z.toInt(),
                        level.toInt(),
                        shape.toInt(),
                    )
                }
                .toHashSet()
        }
    }

    private fun ByteArrayOutputStream.writeShortSmart(value: Int) {
        if (value <= SMART_BYTE_MAX) {
            write(value)
        } else {
            val short = value or SMART_SHORT_FLAG
            write(short shr 8)
            write(short)
        }
    }

    private fun ByteArrayOutputStream.writeIncrShortSmart(value: Int) {
        var remaining = value
        while (remaining >= INCR_SMART_STEP) {
            writeShortSmart(INCR_SMART_STEP)
            remaining -= INCR_SMART_STEP
        }
        writeShortSmart(remaining)
    }

    class Result(val data: ByteArray, val removed: Int)

    private data class ClearedLoc(val id: Int, val x: Int, val z: Int, val level: Int, val shape: Int)
}
