package ru.oftendev.itsmymeta.target

import net.william278.husktowns.api.HuskTownsAPI
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class TargetHuskTowns(override val player: OfflinePlayer,
                      override val targetType: TargetType = TargetType.HUSKTOWNS
) : ITarget {
    override fun getUniqueId(): String {
        return HuskTownsAPI.getInstance().towns.firstOrNull {
            it.members.keys.contains(player.uniqueId)
        }?.id?.toString() ?: "emptytown"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return HuskTownsAPI.getInstance().towns.firstOrNull {
            it.members.keys.contains(player.uniqueId)
        }?.members?.keys?.mapNotNull { Bukkit.getOfflinePlayer(it) } ?: emptyList()
    }
}