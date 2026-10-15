package org.rsmod.game.entity.player

import com.fasterxml.jackson.dataformat.toml.TomlMapper
import dev.or2.central.account.Rights
import java.nio.file.Files
import java.nio.file.Path
import org.rsmod.game.entity.Player

public class OwnerRoster(names: Collection<String>) {
    private val owners: Set<String> = names.mapTo(hashSetOf(), ::normalize)

    public fun isOwner(loginName: String): Boolean = normalize(loginName) in owners

    public companion object {
        public val FILE: Path = Path.of(".data", "owners.toml")

        public val EMPTY: OwnerRoster = OwnerRoster(emptyList())

        private val mapper = TomlMapper()

        @Volatile private var cached: OwnerRoster = EMPTY

        @Volatile private var cachedStamp: Long = Long.MIN_VALUE

        public fun parse(toml: String): OwnerRoster {
            val names = mapper.readTree(toml).path("owners").mapNotNull { it.textValue() }
            return OwnerRoster(names)
        }

        @Synchronized
        public fun current(file: Path = FILE): OwnerRoster {
            if (!Files.isRegularFile(file)) {
                cached = EMPTY
                cachedStamp = Long.MIN_VALUE
                return EMPTY
            }
            val stamp = Files.getLastModifiedTime(file).toMillis()
            if (stamp != cachedStamp) {
                cached = parse(Files.readString(file))
                cachedStamp = stamp
            }
            return cached
        }

        private fun normalize(name: String): String =
            name.trim().lowercase().replace('_', ' ').replace('-', ' ')
    }
}

public fun Player.applyOwnerRole(roster: OwnerRoster = OwnerRoster.current()) {
    isOwner = roster.isOwner(username)
    if (isOwner) {
        modLevel = Rights.ADMINISTRATOR
    }
}
