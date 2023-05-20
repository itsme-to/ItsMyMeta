package ru.oftendev.itsmymeta.commands


import com.willfp.eco.core.command.impl.Subcommand
import com.willfp.eco.util.formatEco
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import ru.oftendev.itsmymeta.ItsMyMeta
import ru.oftendev.itsmymeta.meta.Metas

class ListCommand(plugin: ItsMyMeta): Subcommand(
    plugin,
    "list",
    "itsmymeta.list",
    false
) {
    override fun onExecute(sender: CommandSender, args: MutableList<String>) {
        plugin.langYml.getStrings("meta-list").forEach {
                if (it.contains("%meta%", true)) {
                    Metas.enabledMetas.forEach { meta ->
                        sender.sendMessage(
                            it.replace("%meta%", meta.id)
                                .replace("%type%", meta.type.id)
                                .replace("%team%", meta.partyMode.id)
                                .formatEco(sender as? Player)
                        )
                    }
                } else {
                    sender.sendMessage(it.formatEco(sender as? Player))
                }
            }
    }

    fun multilineComponent(components: List<Component>): Component {
        var result = Component.empty()
        for (i in 0 until components.size-1) {
            result = result.append(components[i])
            result = result.append(Component.newline())
        }
        result = result.append(components.last())
        return result
    }
}