package org.rsmod.content.other.skillingzone.zones

val SkillingHubZone =
    Zone(
        name = "hub",
        walls = emptyList(),
        resources = XpBoardResources,
        decorations = emptyList(),
        npcs = listOf(spawned("npc.prestige_master", 3263, 6071)),
    )
