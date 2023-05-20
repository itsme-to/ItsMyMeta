package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.meta.Metas
import ru.oftendev.itsmymeta.meta.enum.StatType
import ru.oftendev.itsmymeta.meta.getOfPlayer
import ru.oftendev.itsmymeta.meta.giveMeta
import ru.oftendev.itsmymeta.meta.resetMeta

class ResetCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "reset",
    "itsmymeta.reset",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        val player = getOfPlayer(args.getOrNull(0) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-player"))
            return
        }) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-player"))
            return
        }

        val meta = Metas.getMeta(args.getOrNull(1) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-meta"))
            return
        }) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("invalid-meta"))
            return
        }

        player.resetMeta(meta)

        sender.sendMessage(plugin.langYml.getFormattedString("messages.prefix") +
                plugin.langYml.getString("messages.meta-reset")
            .replace("%player%", player.savedDisplayName)
            .replace("%meta%", meta.id)
            .formatEco(player.player)
        )
    }

    override fun tabComplete(sender: CommandSender, args: MutableList<String>): MutableList<String> {
        return when(args.size) {
            1 -> StringUtil.copyPartialMatches(args[0], Bukkit.getOnlinePlayers().map { it.name }.toMutableList(),
                mutableListOf())
            2 -> StringUtil.copyPartialMatches(args[1], Metas.enabledMetas.map { it.id }.toMutableList(),
                mutableListOf())
            else -> mutableListOf()
        }
    }
}