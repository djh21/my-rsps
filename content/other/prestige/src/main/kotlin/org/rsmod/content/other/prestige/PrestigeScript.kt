package org.rsmod.content.other.prestige

import jakarta.inject.Inject
import org.rsmod.api.player.dialogue.Dialogue
import org.rsmod.api.player.output.mes
import org.rsmod.api.player.protect.ProtectedAccessLauncher
import org.rsmod.api.player.stat.PlayerXpMultiplier
import org.rsmod.api.player.stat.PlayerXpMultipliers
import org.rsmod.api.player.stat.statBase
import org.rsmod.api.script.onCommand
import org.rsmod.api.script.onOpNpc1
import org.rsmod.api.table.PrestigeBonusesRow
import org.rsmod.game.cheat.Cheat
import org.rsmod.game.entity.Npc
import org.rsmod.game.entity.Player
import org.rsmod.plugin.scripts.PluginScript
import org.rsmod.plugin.scripts.ScriptContext

class PrestigeScript @Inject constructor(private val protectedAccess: ProtectedAccessLauncher) :
    PluginScript() {
    override fun ScriptContext.startup() {
        PlayerXpMultipliers.provider = PlayerXpMultiplier { player, stat ->
            PrestigeBonusesRow.byName(stat)?.let { player.prestigeMultiplier(it) } ?: 1.0
        }

        onOpNpc1(PRESTIGE_MASTER) { startDialogue(it.npc) { masterMenu(it.npc) } }

        onCommand("prestige") {
            desc = "Prestige a skill: ::prestige <skill>"
            cheat { prestigeCommand() }
        }

        onCommand("prestigelist") {
            desc = "List your prestige level and XP bonus for every skill"
            cheat { player.listPrestige() }
        }
    }

    private fun Cheat.prestigeCommand() {
        val name = args.joinToString(" ")
        if (name.isBlank()) {
            player.mes("Usage: ::prestige <skill>")
            return
        }
        protectedAccess.launch(player) { startDialogue { confirmAndPrestige(name) } }
    }

    private suspend fun Dialogue.masterMenu(npc: Npc) {
        chatNpc(happy, "Greetings, adventurer. Have you mastered a skill and want more?")
        while (true) {
            when (
                choice4(
                    "What is prestige?",
                    1,
                    "Show my prestige levels.",
                    2,
                    "I want to prestige a skill.",
                    3,
                    "Goodbye.",
                    4,
                )
            ) {
                1 -> explain()
                2 -> {
                    player.listPrestige()
                    chatNpc(neutral, "I have listed your prestige levels in your chat box.")
                }
                3 -> prestigeSkill()
                else -> return
            }
        }
    }

    private suspend fun Dialogue.explain() {
        chatNpc(
            neutral,
            "Reach level 99 in a skill and I can reset it to the start. In return that skill " +
                "earns permanently more experience.",
        )
        chatNpc(
            neutral,
            "Each skill has its own bonus and its own maximum prestige. Use ::prestigelist to " +
                "see them all.",
        )
    }

    private suspend fun Dialogue.prestigeSkill() {
        val name = access.stringDialog("Which skill do you want to prestige?")
        confirmAndPrestige(name)
    }

    private suspend fun Dialogue.confirmAndPrestige(name: String) {
        val row = PrestigeBonusesRow.byName(name)
        if (row == null) {
            mesbox("There is no skill called '$name'.")
            return
        }
        val prestige = player.prestigeLevel(row)
        if (prestige >= row.maxPrestige) {
            mesbox("${row.displayName} is already at its maximum prestige.")
            return
        }
        if (player.statBase(row.stat) < PrestigeMath.REQUIRED_LEVEL) {
            mesbox("You need level ${PrestigeMath.REQUIRED_LEVEL} ${row.displayName} to prestige.")
            return
        }
        mesbox(
            "Prestiging ${row.displayName} resets it to the starting level and 0 experience. " +
                "You gain +${row.bonusPercent}% ${row.displayName} experience per prestige."
        )
        val confirmed = choice2("Yes, reset ${row.displayName}.", true, "No, keep my levels.", false)
        if (!confirmed) return
        when (player.prestige(row)) {
            PrestigeOutcome.Success ->
                player.mes(
                    "You prestige ${row.displayName} to ${prestige + 1}. Your experience bonus is " +
                        "now +${player.prestigeBonusPercent(row)}%."
                )
            PrestigeOutcome.BelowRequiredLevel ->
                player.mes("You need level ${PrestigeMath.REQUIRED_LEVEL} to prestige.")
            PrestigeOutcome.MaxPrestige -> player.mes("That skill is already at maximum prestige.")
        }
    }

    private fun Player.listPrestige() {
        for (row in PrestigeBonusesRow.all()) {
            mes(
                "${row.displayName}: prestige ${prestigeLevel(row)}/${row.maxPrestige} " +
                    "(+${prestigeBonusPercent(row)}% XP)"
            )
        }
    }

    private companion object {
        private const val PRESTIGE_MASTER = "npc.prestige_master"
    }
}
