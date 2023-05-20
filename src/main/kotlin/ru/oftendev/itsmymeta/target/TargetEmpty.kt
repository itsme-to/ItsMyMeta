package ru.oftendev.itsmymeta.target

import org.bukkit.OfflinePlayer

class TargetEmpty(override val player: OfflinePlayer, override val targetType: TargetType = TargetType.EMPTY) : ITarget {
    override fun getUniqueId(): String {
        return "empty"
    }

    override fun getApplicablePlayers(): Collection<OfflinePlayer> {
        return listOf()
    }
}