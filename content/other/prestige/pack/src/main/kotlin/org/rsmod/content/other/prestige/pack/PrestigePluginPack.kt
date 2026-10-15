package org.rsmod.content.other.prestige.pack

import dev.openrune.definition.dbtables.DBTable
import dev.openrune.pack.PluginPack

class PrestigePluginPack : PluginPack() {
    override fun dbTables(): List<DBTable> = listOf(PrestigeTable.bonuses())
}
