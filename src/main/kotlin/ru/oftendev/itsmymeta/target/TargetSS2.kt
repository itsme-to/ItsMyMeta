package ru.oftendev.itsmymeta.target

import com.bgsoftware.superiorskyblock.api.SuperiorSkyblockAPI
import org.bukkit.OfflinePlayer

class TargetSS2(override val player: OfflinePlayer,
                override val targetType: TargetType = TargetType.SS2
) : ITarget {
    override fun getUniqueId(): String {
        return SuperiorSkyblockAPI.getPlayer(player.uniqueId).island?.uniqueId?.toString() ?: "emptyisland"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return SuperiorSkyblockAPI.getPlayer(player.uniqueId).island
            ?.getIslandMembers(true)?.mapNotNull { it.asOfflinePlayer() } ?: emptyList()
    }
}