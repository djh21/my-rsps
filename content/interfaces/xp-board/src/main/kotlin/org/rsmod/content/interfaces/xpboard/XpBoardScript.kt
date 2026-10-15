package org.rsmod.content.interfaces.xpboard

import dev.openrune.rscm.RSCM.asRSCM
import dev.openrune.rscm.RSCMType
import jakarta.inject.Inject
import java.util.WeakHashMap
import org.rsmod.api.player.output.runClientScript
import org.rsmod.api.player.protect.ProtectedAccess
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.ui.ifSetText
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onIfClose
import org.rsmod.api.script.onIfModalButton
import org.rsmod.api.script.onOpLoc1
import org.rsmod.game.cheat.Cheat
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

private const val INTERFACE = "interface.xp_board"
private const val BOARD_LOC = "loc.hiscores_board"

private class BoardState(val schema: String) {
    var filter: HighscoreFilter = HighscoreFilters.TOTAL
    var page: Int = 0
    var rows: List<HighscoreRow>? = null
    var request: Int = 0
}

class XpBoardScript
@Inject
internal constructor(
    private val service: HighscoresService,
    private val protectedAccess: ProtectedAccessLauncher,
) : PluginScript() {
    private val states = WeakHashMap<Player, BoardState>()

    override fun ScriptContext.startup() {
        for (command in listOf("xpboard", "highscores")) {
            onCommand(command) {
                desc = "Open the XP board"
                cheat { openBoard() }
            }
        }

        onOpLoc1(BOARD_LOC) { open() }

        onIfClose(INTERFACE) { states.remove(player) }

        HighscoreFilters.ALL.forEachIndexed { index, filter ->
            onIfModalButton("component.xp_board:filter$index") { select(filter) }
        }
        onIfModalButton("component.xp_board:prev") { turnPage(-1) }
        onIfModalButton("component.xp_board:next") { turnPage(1) }
    }

    private fun Cheat.openBoard() {
        protectedAccess.launch(player) { open() }
    }

    private fun ProtectedAccess.open() {
        val state = BoardState(player.worldType.key)
        states[player] = state
        ifOpenMainModal(INTERFACE)
        load(player, state)
    }

    private fun ProtectedAccess.select(filter: HighscoreFilter) {
        val state = states[player] ?: return
        state.filter = filter
        state.page = 0
        load(player, state)
    }

    private fun ProtectedAccess.turnPage(delta: Int) {
        val state = states[player] ?: return
        val rows = state.rows ?: return
        state.page = clampPage(state.page + delta, rows.size)
        render(player, state)
    }

    private fun load(player: Player, state: BoardState) {
        val request = ++state.request
        state.rows = null
        renderFilters(player, state)
        showMessage(player, state, "Loading...")
        service.load(state.schema, state.filter) { rows ->
            if (states[player] !== state || state.request != request) {
                return@load
            }
            state.rows = rows
            render(player, state)
        }
    }

    private fun render(player: Player, state: BoardState) {
        val rows = state.rows
        if (rows == null) {
            showMessage(player, state, "Highscores unavailable")
            return
        }
        if (rows.isEmpty()) {
            showMessage(player, state, "No players ranked yet")
            return
        }
        val page = clampPage(state.page, rows.size)
        val shown = pageSlice(rows, page)
        player.ifSetText("component.xp_board:status", "")
        for (slot in 0 until PAGE_SIZE) {
            val row = shown.getOrNull(slot)
            player.ifSetText("component.xp_board:rank$slot", row?.rank?.toString().orEmpty())
            player.ifSetText("component.xp_board:name$slot", row?.name.orEmpty())
            player.ifSetText("component.xp_board:level$slot", row?.level?.toString().orEmpty())
            player.ifSetText("component.xp_board:xp$slot", row?.let { formatXp(it.xp) }.orEmpty())
        }
        player.ifSetText("component.xp_board:pagelabel", "Page ${page + 1}/${pageCount(rows.size)}")
    }

    private fun showMessage(player: Player, state: BoardState, message: String) {
        for (slot in 0 until PAGE_SIZE) {
            player.ifSetText("component.xp_board:rank$slot", "")
            player.ifSetText("component.xp_board:name$slot", "")
            player.ifSetText("component.xp_board:level$slot", "")
            player.ifSetText("component.xp_board:xp$slot", "")
        }
        player.ifSetText("component.xp_board:status", message)
        player.ifSetText("component.xp_board:pagelabel", "")
        player.ifSetText("component.xp_board:skilllabel", "Skill: ${state.filter.title}")
    }

    private fun renderFilters(player: Player, state: BoardState) {
        player.runClientScript(
            "clientscript.xp_board_buttons".asRSCM(RSCMType.CLIENTSCRIPT),
            HighscoreFilters.ALL.indexOf(state.filter),
        )
        player.ifSetText("component.xp_board:skilllabel", "Skill: ${state.filter.title}")
    }
}
