package ru.oftendev.itsmymeta.meta

import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.config.toConfig
import com.willfp.eco.core.registry.Registry
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import com.willfp.libreforge.loader.configs.LegacyLocation

object Metas: ConfigCategory("meta", "metas") {
    private val registry = Registry<Meta>()

    override val legacyLocation = LegacyLocation(
        "config.yml",
        "meta"
    )

    @JvmStatic
    val metas
        get() = registry.values()

    @JvmStatic
    val enabledMetas: List<Meta>
        get() = metas.filter { it.partyMode.enabled }

    @JvmStatic
    fun getMeta(id: String): Meta? {
        return enabledMetas.find { it.id == id }
    }

    override fun acceptConfig(plugin: LibreforgePlugin, id: String, config: Config) {
        registry.register(Meta(config))
    }

    override fun clear(plugin: LibreforgePlugin) {
        registry.clear()
    }

    override fun afterReload(plugin: LibreforgePlugin) {
        plugin.logger.info(
            "&fLoaded &3${enabledMetas.size} (${metas.size}) &fmetas with " +
                    "&3${enabledMetas.sumOf { it.stats.size }} &ftotal stats"
        )
    }
}