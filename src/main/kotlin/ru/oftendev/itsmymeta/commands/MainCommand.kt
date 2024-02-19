package ru.oftendev.itsmymeta.commands

import com.willfp.eco.core.command.impl.PluginCommand
import org.bukkit.command.CommandSender
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.commands.currency.CommandCurrency

class MainCommand(plugin: ItsMyMeta): PluginCommand(
    plugin,
    "itsmymeta",
    "itsmymeta.use",
    false
) {
    init {
        this.addSubcommand(ReloadCommand(plugin))
            .addSubcommand(GiveCommand(plugin))
            .addSubcommand(TakeCommand(plugin))
            .addSubcommand(HelpCommand(plugin))
            .addSubcommand(SetCommand(plugin))
            .addSubcommand(InfoCommand(plugin))
            .addSubcommand(ListCommand(plugin))
            .addSubcommand(CommandCurrency(plugin))
            .addSubcommand(ResetCommand(plugin))
            .addSubcommand(LeaderboardCommand(plugin))
    }

    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        sender.sendMessage(plugin.langYml.getMessage("invalid-command"))
    }
}