package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.StringUtils
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.meta.getBalance

class CommandBalance(
    plugin: EcoPlugin
) : Subcommand(
    plugin,
    "balance",
    "itsmymeta.command.balance",
    true
) {
    override fun onExecute(player: Player, args: List<String>) {
        if (args.isEmpty()) {
            player.sendMessage(plugin.langYml.getMessage("requires-currency"))
            return
        }

        val currency = Metas.enabledMetas.mapNotNull { it.currency }.firstOrNull {
            it.id.equals(args[0].lowercase(), true)
        }

        if (currency == null) {
            player.sendMessage(plugin.langYml.getMessage("invalid-currency"))
            return
        }

        player.sendMessage(
            plugin.langYml.getMessage("balance", StringUtils.FormatOption.WITHOUT_PLACEHOLDERS)
                .replace("%amount%", player.getBalance(currency).toNiceString())
                .replace("%currency%", currency.name)
        )
    }

    override fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
        val completions = mutableListOf<String>()

        if (args.isEmpty()) {
            Metas.enabledMetas.mapNotNull { it.currency }.map { it.id }
        }

        if (args.size == 1) {
            StringUtil.copyPartialMatches(
                args[0],
                Metas.enabledMetas.mapNotNull { it.currency }.map { it.id },
                completions
            )
        }

        return completions
    }
}
