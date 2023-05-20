package ru.oftendev.itsmymeta.target

import org.bukkit.OfflinePlayer

interface ITarget {
    val player: OfflinePlayer
    val targetType: TargetType
    fun getUniqueId(): String
    val id: String
        get() = "${targetType.id}:${getUniqueId()}"
    fun getApplicablePlayers(): Collection<OfflinePlayer>
    fun isApplicableFor(player: OfflinePlayer): Boolean {
        return getApplicablePlayers().contains(player)
    }

    companion object {
        @JvmStatic
        fun createTargetFor(player: OfflinePlayer, type: TargetType): ITarget {
            return when(type) {
                TargetType.PLAYER -> TargetPlayer(player)
                TargetType.HUSKTOWNS -> TargetHuskTowns(player)
                TargetType.FUUID -> TargetFUUID(player)
                TargetType.SS2 -> TargetSS2(player)
                TargetType.BENTO_BOX -> TargetBentoBox(player)
                TargetType.GLOBAL -> TargetGlobal(player)
                TargetType.EMPTY -> TargetEmpty(player)
            }
        }
    }
}