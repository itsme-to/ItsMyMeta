package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.meta.resetMeta

class CommandReset(
    plugin: EcoPlugin
) : Subcommand(
    plugin,
    "reset",
    "itsmymeta.command.reset",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            sender.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(args[0])

        if (!player.hasPlayedBefore() && !player.isOnline) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        if (args.size < 2) {
            sender.sendMessage(plugin.langYml.getMessage("requires-currency"))
            return
        }

        val currency = Metas.enabledMetas.mapNotNull { it.currency }.firstOrNull {
            it.id.equals(args[1].lowercase(), true)
        }

        if (currency == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        player.resetMeta(currency.parent)

        sender.sendMessage(
            plugin.langYml.getMessage("reset-currency", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%player%", player.savedDisplayName)
                .replace("%amount%", currency.default.toNiceString())
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
                Metas.enabledMetas.mapNotNull { it.currency }.map { it.id },
                completions
            )
        }

        return completions
    }
}
