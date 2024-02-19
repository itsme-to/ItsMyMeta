package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.savedDisplayName
import com.willfp.eco.util.toNiceString
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.meta.*

class LeaderboardCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "leaderboard",
    "itsmymeta.leaderboard",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {

        val meta = Metas.getMeta(args.getOrNull(0) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-meta"))
            return
        }) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-meta"))
            return
        }

        if (!meta.hasLeaderboard) {
            sender.sendMessage(plugin.langYml.getMessage("invalid-meta"))
            return
        }

        val place = (args.getOrNull(1) ?: run {
            sender.sendMessage(plugin.langYml.getMessage("requires-place"))
            return
        }).toIntOrNull() ?: run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-place"))
            return
        }

        val leader = meta.getTop(place)

        if (leader == null) {
            sender.sendMessage(plugin.langYml.getMessage("leaderboard.place-empty")
                .replace("%place%", place.toNiceString())
                .replace("%meta%", meta.id))
        } else {
            sender.sendMessage(plugin.langYml.getMessage("leaderboard.place")
                .replace("%place%", place.toNiceString())
                .replace("%meta%", meta.id)
                .replace("%name%", leader.player.savedDisplayName)
                .replace("%value%", leader.amount.toNiceString())
            )
        }
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Metas.enabledMetas.filter { it.hasLeaderboard }
                .map { it.id },
                mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], listOf("1", "2", "3", "4", "5"),
                mutableListOf())
            else -> mutableListOf()
        }
    }
}