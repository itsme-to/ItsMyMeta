package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.meta.MetaCurrency

class LeaderboardCurrencyCommand(plugin: EcoPlugin, val currency: MetaCurrency): Subcommand(
    plugin,
    "leaderboard",
    "itsmymeta.command.${currency.id}.leaderboard",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        val place = (args.getOrNull(0) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("requires-place"))
            return
        }).toIntOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-place"))
            return
        }

        val leader = currency.parent.getTop(place)

        if (leader == null) {
            sender.sendMessage(plugin.langYml.getMessage("leaderboard.place-empty")
                .replace("%place%", place.toNiceString())
                .replace("%meta%", currency.id))
        } else {
            sender.sendMessage(plugin.langYml.getMessage("leaderboard.place")
                .replace("%place%", place.toNiceString())
                .replace("%meta%", currency.id)
                .replace("%name%", leader.player.savedDisplayName)
                .replace("%value%", leader.amount.toNiceString())
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], listOf("1", "2", "3", "4", "5"),
                mutableListOf())
            else -> mutableListOf()
        }
    }
}