package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmymeta.ItsMyMeta

class HelpCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "help",
    "itsmymeta.help",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        plugin.langYml.getFormattedStrings("help")
            .forEach {
                sender.sendMessage(it)
            }
    }
}