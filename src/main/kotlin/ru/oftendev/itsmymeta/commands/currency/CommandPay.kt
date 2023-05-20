package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.meta.adjustBalance
import ru.oftendev.itsmymeta.meta.getBalance
import kotlin.math.ceil
import kotlin.math.floor

class CommandPay(
    plugin: EcoPlugin
) : Subcommand(
    plugin,
    "pay",
    "itsmymeta.command.pay",
    true
) {
    override fun onExecute(player: Player, args: List<String>) {
        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }

        @Suppress("DEPRECATION")
        val recipient = Bukkit.getOfflinePlayer(args[0])

        if ((!recipient.hasPlayedBefore() && !recipient.isOnline) || (recipient.uniqueId == player.uniqueId)) {
            player.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        if (args.size < 2) {
            player.sendMessage(plugin.langYml.getMessage("requires-currency"))
            return
        }

        val currency = Metas.enabledMetas.mapNotNull { it.currency }.firstOrNull {
            it.id.equals(args[1].lowercase(), true)
        }

        if (currency == null || !currency.isPayable) {
            player.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        if (args.size < 3) {
            player.sendMessage(plugin.langYml.getMessage("requires-value"))
            return
        }

        val amount = args[2].toDoubleOrNull()

        if (amount == null || amount <= 0) {
            player.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        if (ceil(amount) != floor(amount) && !currency.isDecimal) {
            player.sendMessage(plugin.langYml.getMessage("invalid-amount"))
            return
        }

        if (player.getBalance(currency) < amount) {
            player.sendMessage(plugin.langYml.getMessage("cannot-afford"))
            return
        }

        if (recipient.getBalance(currency) + amount > currency.max) {
            player.sendMessage(plugin.langYml.getMessage("too-much"))
            return
        }

        recipient.adjustBalance(currency, amount)
        player.adjustBalance(currency, -amount)

        player.sendMessage(
            plugin.langYml.getMessage("paid-player", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", recipient.savedDisplayName)
                .replace("%amount%", amount.toNiceString())
                .replace("%currency%", currency.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            return Bukkit.getOnlinePlayers().map { it.name }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Bukkit.getOnlinePlayers().map { it.name },
                completions
            )
        }

        if (args.size == 2) {
            StringUtil.copyPartialMatches(
                args[1],
                Metas.enabledMetas.mapNotNull { it.currency }.filter { it.isPayable }.map { it.id },
                completions
            )
        }

        if (args.size == 3) {
            StringUtil.copyPartialMatches(
                args[2],
                arrayOf(1, 2, 3, 4, 5).map { it.toString() },
                completions
            )
        }

        return completions
    }
}
