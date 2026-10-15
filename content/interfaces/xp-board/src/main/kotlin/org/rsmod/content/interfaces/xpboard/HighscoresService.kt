package org.rsmod.content.interfaces.xpboard

import com.github.michaelbull.logging.InlineLogger
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.rsmod.api.db.DatabaseConnection
import org.rsmod.api.db.gateway.GameDbManager
import org.rsmod.api.db.gateway.model.GameDbResult

@Singleton
internal class HighscoresService @Inject constructor(private val db: GameDbManager) {
    private val logger = InlineLogger()

    private class Cached(val rows: List<HighscoreRow>, val loadedAt: Long)

    private val cache = HashMap<String, Cached>()
    private val waiting = HashMap<String, MutableList<(List<HighscoreRow>?) -> Unit>>()

    /** Game thread only; [onLoaded] gets `null` when the database could not be queried. */
    fun load(schema: String, filter: HighscoreFilter, onLoaded: (List<HighscoreRow>?) -> Unit) {
        val key = "$schema/${filter.title}"
        val hit = cache[key]
        if (hit != null && System.currentTimeMillis() - hit.loadedAt < CACHE_MILLIS) {
            onLoaded(hit.rows)
            return
        }
        val queue = waiting[key]
        if (queue != null) {
            queue += onLoaded
            return
        }
        waiting[key] = mutableListOf(onLoaded)
        db.request(
            request = { connection -> GameDbResult.Ok(query(connection, schema, filter)) },
            response = { result ->
                val callbacks = waiting.remove(key).orEmpty()
                val rows =
                    when (result) {
                        is GameDbResult.Ok -> result.value
                        is GameDbResult.Err -> {
                            logger.warn { "Highscores query failed for $key: $result" }
                            null
                        }
                    }
                if (rows != null) {
                    cache[key] = Cached(rows, System.currentTimeMillis())
                }
                callbacks.forEach { it(rows) }
            },
        )
    }

    private fun query(
        connection: DatabaseConnection,
        schema: String,
        filter: HighscoreFilter,
    ): List<HighscoreRow> {
        require(SCHEMA_NAME.matches(schema)) { "Not a valid schema identifier: '$schema'" }
        val statId = filter.statId
        val sql = if (statId == null) totalSql(schema) else skillSql(schema)
        val raw = ArrayList<RawHighscore>(MAX_ENTRIES)
        connection.prepareStatement(sql).use { ps ->
            if (statId == null) {
                ps.setInt(1, MAX_ENTRIES)
            } else {
                ps.setInt(1, statId)
                ps.setInt(2, MAX_ENTRIES)
            }
            ps.executeQuery().use { rs ->
                while (rs.next()) {
                    raw += RawHighscore(rs.getString(1), rs.getInt(2), rs.getLong(3))
                }
            }
        }
        return rankRows(raw)
    }

    private fun totalSql(schema: String): String {
        val ids = HighscoreFilters.SKILL_STAT_IDS.joinToString(",")
        return """
            SELECT c.display_name, SUM(s.base_level), SUM(s.fine_xp::bigint)
            FROM "$schema".stats s
            JOIN public.account_characters c ON c.id = s.character_id
            WHERE s.stat_id IN ($ids) AND c.display_name IS NOT NULL
            GROUP BY c.id, c.display_name
            ORDER BY 2 DESC, 3 DESC, c.id ASC
            LIMIT ?
        """
            .trimIndent()
    }

    private fun skillSql(schema: String): String =
        """
            SELECT c.display_name, s.base_level, s.fine_xp::bigint
            FROM "$schema".stats s
            JOIN public.account_characters c ON c.id = s.character_id
            WHERE s.stat_id = ? AND c.display_name IS NOT NULL
            ORDER BY s.base_level DESC, s.fine_xp DESC, c.id ASC
            LIMIT ?
        """
            .trimIndent()

    private companion object {
        private const val CACHE_MILLIS = 60_000L
        private val SCHEMA_NAME = Regex("^[a-z][a-z0-9_]{0,62}$")
    }
}
