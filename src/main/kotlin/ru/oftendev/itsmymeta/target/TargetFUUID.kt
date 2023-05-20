package ru.oftendev.itsmymeta.target

import com.massivecraft.factions.Factions
import org.bukkit.OfflinePlayer

class TargetFUUID(override val player: OfflinePlayer,
                  override val targetType: TargetType = TargetType.FUUID
) : ITarget {
    override fun getUniqueId(): String {
        return Factions.getInstance().allFactions.firstOrNull {
            it.fPlayers.any { fpl -> fpl.offlinePlayer.uniqueId.equals(player.uniqueId) }
        }?.id ?: "emptyfaction"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return Factions.getInstance().allFactions.firstOrNull {
            it.fPlayers.any { fpl -> fpl.offlinePlayer.uniqueId.equals(player.uniqueId) }
        }?.fPlayers?.mapNotNull { it.offlinePlayer } ?: emptyList()
    }
}