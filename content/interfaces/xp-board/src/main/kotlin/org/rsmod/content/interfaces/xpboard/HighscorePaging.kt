package org.rsmod.content.interfaces.xpboard

internal const val PAGE_SIZE = 15
internal const val MAX_ENTRIES = 100
internal const val FINE_XP_PER_XP = 10

internal data class HighscoreRow(val rank: Int, val name: String, val level: Int, val xp: Long)

internal data class RawHighscore(val name: String, val level: Int, val fineXp: Long)

internal fun rankRows(raw: List<RawHighscore>): List<HighscoreRow> =
    raw.mapIndexed { index, entry ->
        HighscoreRow(index + 1, entry.name, entry.level, entry.fineXp / FINE_XP_PER_XP)
    }

internal fun pageCount(total: Int): Int =
    if (total <= 0) 1 else (total + PAGE_SIZE - 1) / PAGE_SIZE

internal fun clampPage(page: Int, total: Int): Int = page.coerceIn(0, pageCount(total) - 1)

internal fun pageSlice(rows: List<HighscoreRow>, page: Int): List<HighscoreRow> {
    val start = clampPage(page, rows.size) * PAGE_SIZE
    return rows.drop(start).take(PAGE_SIZE)
}

internal fun formatXp(xp: Long): String = "%,d".format(xp)
