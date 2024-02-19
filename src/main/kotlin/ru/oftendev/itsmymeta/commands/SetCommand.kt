package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.formatEco
import com.willfp.eco.util.savedDisplayName
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.util.StringUtil
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.meta.*
import ru.oftendev.itsmymeta.meta.enum.StatType

class SetCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "set",
    "itsmymeta.set",
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

        if (!player.canUse(meta)) {
            sender.sendMessage(plugin.langYml.getMessage("cant-use-meta"))
            return
        }

        val metaValue = (if (args.size < 3) null else args.subList(2, args.size
        ).joinToString(" ")) ?: kotlin.run {
            sender.sendMessage(plugin.langYml.getMessage("requires-value"))
            return
        }

        when(meta.type) {
            StatType.STRING -> player.setMeta(meta, metaValue)
            StatType.INTEGER -> player.setMeta(meta, metaValue.toIntOrNull() ?: kotlin.run {
                sender.sendMessage(plugin.langYml.getMessage("invalid-value"))
                return
            })
            StatType.DOUBLE -> player.setMeta(meta, metaValue.toDoubleOrNull() ?: kotlin.run {
                sender.sendMessage(plugin.langYml.getMessage("invalid-value"))
                return
            })
        }

        if (args.lastOrNull()?.equals("-s", true) == true) return
        sender.sendMessage(plugin.langYml.getFormattedString("messages.prefix") +
                plugin.langYml.getString("messages.meta-set")
            .replace("%player%", player.savedDisplayName)
            .replace("%meta%", meta.id)
            .replace("%value%", metaValue)
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