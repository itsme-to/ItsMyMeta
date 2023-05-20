package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmymeta.ItsMyMeta

class ReloadCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "reload",
    "itsmymeta.reload",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        this.plugin.reload()
        sender.sendMessage(plugin.langYml.getMessage("reloaded"))
    }
}