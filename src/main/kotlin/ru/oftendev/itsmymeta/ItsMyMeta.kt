package ru.oftendev.itsmymeta

import com.willfp.ItsMyMeta.integrations.IntegrationVault
import com.willfp.eco.core.command.impl.PluginCommand
import com.willfp.eco.core.integrations.IntegrationLoader
import com.willfp.eco.util.toNiceString
import com.willfp.libreforge.loader.LibreforgePlugin
import com.willfp.libreforge.loader.configs.ConfigCategory
import org.bukkit.event.Listener
import ru.oftendev.itsmymeta.commands.MainCommand
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.target.TargetType
import java.text.DecimalFormat
import java.util.*


class ItsMyMeta: LibreforgePlugin() {
    val serverID = configYml.getString("server-id")

    init {
        instance = this
    }

    /**
     * All listeners to be registered.
     *
     * @return A list of all listeners.
     */
    override fun loadListeners(): MutableList<Listener> {
        return mutableListOf()
    }

    override fun loadPluginCommands(): MutableList<PluginCommand> {
        return mutableListOf(MainCommand(this))
    }

    override fun loadIntegrationLoaders(): List<IntegrationLoader> {
        return TargetType.getIntegrationLoaders() + listOf(
            IntegrationLoader("Vault") { IntegrationVault.isVaultPresent = true }
        )
    }

    override fun loadConfigCategories(): List<ConfigCategory> {
        return listOf(Metas)
    }

    override fun handleEnable() {
        junk()
    }

    private fun junk() {
        this.configYml.getSubsections("meta").forEach {
            Metas.acceptConfig(this, it.getString("id"), it)
        }
    }

    override fun handleReload() {
        junk()
    }

    companion object {
        @JvmStatic
        lateinit var instance: ItsMyMeta
            private set
    }
}

private val suffixes: NavigableMap<Long, String> = TreeMap<Long, String>().apply {
    this[1_000L] = "k"
    this[1_000_000L] = "M"
    this[1_000_000_000L] = "B"
    this[1_000_000_000_000L] = "T"
    this[1_000_000_000_000_000L] = "Q"
}

private val FIXED_FORMAT = DecimalFormat("#")

fun Number.stringFormat(): String {
    val newThis = this.toLong()

    if (newThis == Long.MIN_VALUE) {
        return (Long.MIN_VALUE + 1).stringFormat()
    }
    if (newThis < 0) {
        return "-" + (-newThis).stringFormat()
    }
    if (newThis < 1000) {
        return this.toString() //deal with easy case
    }
    val e: Map.Entry<Long, String> = suffixes.floorEntry(newThis)
    val divideBy: Long = e.key
    val suffix: String = e.value
    val truncated = newThis / (divideBy / 10) //the number part of the output times 10
    val hasDecimal = truncated < 100 && truncated / 10.0 != (truncated / 10).toDouble()
    return (if (hasDecimal) (truncated / 10.0).toNiceString() + suffix else (truncated / 10.0).toNiceString() + suffix)
        .removeSuffix(".0")
}

fun Number.fixedFormat(): String {
    return FIXED_FORMAT.format(this.toDouble())
}