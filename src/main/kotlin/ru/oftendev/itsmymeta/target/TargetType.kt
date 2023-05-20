package ru.oftendev.itsmymeta.target

import com.willfp.eco.core.integrations.IntegrationLoader
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

enum class TargetType(val id: String, var enabled: Boolean = false) {
    PLAYER("player", true),
    BENTO_BOX("bentobox"),
    FUUID("factionsuuid"),
    GLOBAL("global", true),
    HUSKTOWNS("husktowns"),
    SS2("superiorskyblock2"),
    EMPTY("empty", true);

    fun getParty(player: OfflinePlayer): ITarget {
        return when(id) {
            "player" -> TargetPlayer(player)
            "bentobox" -> TargetBentoBox(player)
            "factionsuuid" -> TargetFUUID(player)
            "global" -> TargetGlobal(player)
            "husktowns" -> TargetHuskTowns(player)
            else -> TargetSS2(player)
        }
    }

    fun enable() {
        this.enabled = true
    }

    override fun toString(): String {
        return this.id
    }

    companion object {
        @JvmStatic
        fun getById(id: String): TargetType {
            return values().firstOrNull { it.id.equals(id, true) } ?: EMPTY.apply {
                Bukkit.getLogger().warning("Tried to load invalid ItsMyMeta party mode: $id")
            }
        }

        @JvmStatic
        fun getIntegrationLoaders(): List<IntegrationLoader> {
            return listOf(
                IntegrationLoader("BentoBox") { BENTO_BOX.enable() },
                IntegrationLoader("FactionsUUID") { FUUID.enable() },
                IntegrationLoader("HuskTowns") { HUSKTOWNS.enable() },
                IntegrationLoader("SuperiorSkyblock2") { SS2.enable() },
            )
        }
    }
}