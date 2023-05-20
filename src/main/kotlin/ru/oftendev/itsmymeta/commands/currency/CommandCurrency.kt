package ru.oftendev.itsmymeta.commands.currency

import com.willfp.eco.core.EcoPlugin
import com.willfp.eco.core.command.impl.Subcommand
import org.bukkit.command.CommandSender

class CommandCurrency(plugin: EcoPlugin) : Subcommand(plugin, "currency", "itsmymeta.command.currency",
    false) {
    init {
        this.addSubcommand(CommandGive(plugin))
            .addSubcommand(CommandGivesilent(plugin))
            .addSubcommand(CommandGet(plugin))
            .addSubcommand(CommandSet(plugin))
            .addSubcommand(CommandReset(plugin))
            .addSubcommand(CommandPay(plugin))
            .addSubcommand(CommandBalance(plugin))
            .addSubcommand(CommandTake(plugin))
            .addSubcommand(CommandTakesilent(plugin))
    }

    override fun onExecute(sender: CommandSender, args: List<String>) {
        sender.sendMessage(
            plugin.langYml.getMessage("invalid-command")
        )
    }
}
