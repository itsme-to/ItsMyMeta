package ru.oftendev.itsmymeta.target

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import world.bentobox.bentobox.BentoBox

class TargetBentoBox(override val player: OfflinePlayer,
                     override val targetType: TargetType = TargetType.BENTO_BOX
) : ITarget {
    override fun getUniqueId(): String {
        return BentoBox.getInstance().islandsManager.islands
            .firstOrNull { it.memberSet.contains(player.uniqueId) }?.uniqueId ?: "emptyisland"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return BentoBox.getInstance().islandsManager.islands
            .firstOrNull { it.memberSet.contains(player.uniqueId) }?.memberSet
            ?.mapNotNull { Bukkit.getOfflinePlayer(it) } ?: emptyList()
    }
}