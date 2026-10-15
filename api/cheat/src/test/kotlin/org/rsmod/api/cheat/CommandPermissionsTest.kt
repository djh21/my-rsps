package org.rsmod.api.cheat

import dev.or2.central.account.Rights
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CommandPermissionsTest {
    private val permissions =
        CommandPermissions.parse(
            """
            [admin]
            commands = ["Item", "tele"]

            [mod]
            commands = ["mypos"]
            """
                .trimIndent()
        )

    private fun canUse(
        command: String,
        rights: Rights,
        owner: Boolean = false,
        required: Rights? = Rights.ADMINISTRATOR,
    ) = permissions.canUse(owner, rights, command, required)

    @Test
    fun `owner may use every command`() {
        assertTrue(canUse("reboot", Rights.ADMINISTRATOR, owner = true))
    }

    @Test
    fun `admin is limited to the admin list`() {
        assertTrue(canUse("item", Rights.ADMINISTRATOR))
        assertTrue(canUse("tele", Rights.ADMINISTRATOR))
        assertFalse(canUse("reboot", Rights.ADMINISTRATOR))
        assertFalse(canUse("mypos", Rights.ADMINISTRATOR))
    }

    @Test
    fun `mod is limited to the mod list`() {
        assertTrue(canUse("mypos", Rights.MOD))
        assertFalse(canUse("item", Rights.MOD))
    }

    @Test
    fun `plain players never pass a restricted command`() {
        assertFalse(canUse("item", Rights.NONE))
        assertFalse(canUse("mypos", Rights.NONE))
    }

    @Test
    fun `commands without required rights stay public`() {
        assertTrue(canUse("worldtype", Rights.NONE, required = null))
    }

    @Test
    fun `empty lists lock out non owner staff`() {
        val empty = CommandPermissions.parse("[admin]\ncommands = []\n[mod]\ncommands = []")
        assertFalse(empty.canUse(false, Rights.ADMINISTRATOR, "item", Rights.ADMINISTRATOR))
        assertTrue(empty.canUse(true, Rights.ADMINISTRATOR, "item", Rights.ADMINISTRATOR))
    }
}
