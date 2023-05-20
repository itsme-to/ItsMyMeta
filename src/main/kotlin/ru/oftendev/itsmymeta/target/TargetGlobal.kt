package ru.oftendev.itsmymeta.target

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class TargetGlobal(override val player: OfflinePlayer,
                   override val targetType: TargetType = TargetType.GLOBAL
): ITarget {
    override fun getUniqueId(): String {
        return "global"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return Bukkit.getOfflinePlayers().toList()
    }
}