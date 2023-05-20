package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.meta.adjustBalance

class CommandTakesilent(
    plugin: EcoPlugin
) : Subcommand(
    plugin,
    "takesilent",
    "ecobits.command.takesilent",
    false
) {
    override fun onExecute(sender: CommandSender, args: List<String>) {
        if (args.isEmpty()) {
            return
        }

        @Suppress("DEPRECATION")
        val player = Bukkit.getOfflinePlayer(args[0])

        if (!player.hasPlayedBefore() && !player.isOnline) {
            return
        }

        if (args.size < 2) {
            return
        }

        val currency = Metas.enabledMetas.mapNotNull { it.currency }.firstOrNull {
            it.id.equals(args[1].lowercase(), true)
        }

        if (currency == null) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        if (args.size < 3) {
            return
        }

        val amount = args[2].toDoubleOrNull() ?: return

        player.adjustBalance(currency, -amount)
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
