package org.rsmod.game.entity.player

import dev.or2.central.account.Rights
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.rsmod.game.entity.Player

class OwnerRosterTest {
    @Test
    fun `matches login names ignoring case and separators`() {
        val roster = OwnerRoster.parse("owners = [\"Some Name\", \"other_name\"]")
        assertTrue(roster.isOwner("some name"))
        assertTrue(roster.isOwner("SOME_NAME"))
        assertTrue(roster.isOwner("other name"))
        assertFalse(roster.isOwner("somebody else"))
    }

    @Test
    fun `missing owners key means no owners`() {
        assertFalse(OwnerRoster.parse("# nothing here").isOwner("anyone"))
        assertFalse(OwnerRoster.EMPTY.isOwner("anyone"))
    }

    @Test
    fun `owner role overrides lower rights and non owner is untouched`() {
        val roster = OwnerRoster(listOf("boss"))
        val owner = Player().apply { username = "boss" }
        owner.applyOwnerRole(roster)
        assertTrue(owner.isOwner)
        assertEquals(Rights.ADMINISTRATOR, owner.modLevel)

        val mod = Player().apply { username = "helper"; modLevel = Rights.MOD }
        mod.applyOwnerRole(roster)
        assertFalse(mod.isOwner)
        assertEquals(Rights.MOD, mod.modLevel)
    }

    @Test
    fun `revoked owner loses the flag`() {
        val player = Player().apply { username = "boss" }
        player.applyOwnerRole(OwnerRoster(listOf("boss")))
        player.applyOwnerRole(OwnerRoster.EMPTY)
        assertFalse(player.isOwner)
    }
}
