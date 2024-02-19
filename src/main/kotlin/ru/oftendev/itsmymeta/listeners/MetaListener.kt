package ru.oftendev.itsmymeta.listeners

import com.willfp.eco.core.data.profile
import com.willfp.eco.util.NumberUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import ru.oftendev.itsmymeta.meta.Metas

class MetaListener: Listener {
    @EventHandler
    fun handleJoin(event: PlayerJoinEvent) {
        for (meta in Metas.enabledMetas.filter { it.placeholder != null }) {
            event.player.profile.write(meta.cachedValueKey, NumberUtils
                .evaluateExpression(meta.placeholder!!, event.player).toString())
        }
    }
}