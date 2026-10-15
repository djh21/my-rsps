package org.rsmod.api.cheat

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import dev.or2.central.account.Rights
import org.rsmod.game.entity.Player

public class CommandPermissions(adminCommands: Collection<String>, modCommands: Collection<String>) {
    private val admin: Set<String> = adminCommands.mapTo(hashSetOf()) { it.lowercase() }
    private val mod: Set<String> = modCommands.mapTo(hashSetOf()) { it.lowercase() }

    public fun canUse(player: Player, command: String, requiredRights: Rights?): Boolean =
        canUse(player.isOwner, player.modLevel, command, requiredRights)

    public fun canUse(
        isOwner: Boolean,
        rights: Rights,
        command: String,
        requiredRights: Rights?,
    ): Boolean {
        if (requiredRights == null || isOwner) return true
        val allowed =
            when {
                rights.isAtLeast(Rights.ADMINISTRATOR) -> admin
                rights.isAtLeast(Rights.MOD) -> mod
                else -> return false
            }
        return command.lowercase() in allowed
    }

    public companion object {
        public const val RESOURCE: String = "command-permissions.toml"

        private val mapper = TomlMapper()

        public val current: CommandPermissions by lazy { loadResource() }

        public fun parse(toml: String): CommandPermissions {
            val tree = mapper.readTree(toml)
            fun names(rank: String) =
                tree.path(rank).path("commands").mapNotNull { it.textValue() }
            return CommandPermissions(names("admin"), names("mod"))
        }

        private fun loadResource(): CommandPermissions {
            val stream =
                CommandPermissions::class.java.classLoader.getResourceAsStream(RESOURCE)
                    ?: return CommandPermissions(emptyList(), emptyList())
            return stream.use { parse(it.readBytes().decodeToString()) }
        }
    }
}
