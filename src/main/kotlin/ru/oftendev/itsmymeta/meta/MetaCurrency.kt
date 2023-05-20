package ru.oftendev.itsmymeta.meta

import com.willfp.ItsMyMeta.integrations.IntegrationVault
import com.willfp.eco.core.config.interfaces.Config
import com.willfp.eco.core.data.keys.PersistentDataKey
import com.willfp.eco.core.data.keys.PersistentDataKeyType
import com.willfp.eco.core.price.Prices
import net.milkbowl.vault.economy.Economy
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.plugin.ServicePriority
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.commands.DynamicCurrencyCommand
import ru.oftendev.itsmymeta.currency.PriceFactoryCurrency
import ru.oftendev.itsmymeta.meta.enum.StatType
import java.lang.Math.floor
import java.lang.Math.log10
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.pow

class MetaCurrency(
    val plugin: ItsMyMeta,
    val config: Config,
    val parent: Meta
) {
    val id = parent.id.lowercase()

    val default = parent.default.toString().toDoubleOrNull() ?: parent.min

    val name = config.getFormattedString("name")

    val max = config.getDouble("max").let { if (it < 0) Double.MAX_VALUE else it }

    val isPayable = config.getBool("payable")

    val isDecimal = parent.type == StatType.DOUBLE

    val isRegisteredWithVault = config.getBool("vault")

    val commands = config.getStrings("commands").map { DynamicCurrencyCommand(plugin, it, this) }

    fun registerCommands() {
        this.commands.forEach {
            it.register()
        }
    }

    fun unregisterCommands() {
        this.commands.forEach { it.unregister() }
    }

    init {
        Prices.registerPriceFactory(PriceFactoryCurrency(this))

        if (isRegisteredWithVault && IntegrationVault.isVaultPresent) {
            Bukkit.getServer().servicesManager.register(
                Economy::class.java,
                IntegrationVault(this),
                plugin,
                ServicePriority.Highest
            )
        }

        this.unregisterCommands()
        this.registerCommands()
    }
}

fun OfflinePlayer.getBalance(currency: MetaCurrency): Double {
    return this.getMeta(currency.parent).toString().toDoubleOrNull() ?: 0.0
}

fun OfflinePlayer.setBalance(currency: MetaCurrency, amount: Double) {
    this.setMeta(currency.parent, amount)
}

fun OfflinePlayer.adjustBalance(currency: MetaCurrency, amount: Double) {
    if (amount >= 0) {
        this.giveMeta(currency.parent, amount)
    } else {
        this.takeMeta(currency.parent, abs(amount))
    }
}

fun Double.formatWithExtension(): String {
    val suffix = charArrayOf(' ', 'k', 'M', 'B', 'T', 'P', 'E')
    val numValue = this.toLong()
    val value = floor(log10(numValue.toDouble())).toInt()

    val base = value / 3

    return if (value >= 3 && base < suffix.size) {
        DecimalFormat("#0.0").format(numValue / 10.0.pow((base * 3).toDouble())) + suffix[base]
    } else {
        DecimalFormat("#,##0").format(numValue)
    }
}