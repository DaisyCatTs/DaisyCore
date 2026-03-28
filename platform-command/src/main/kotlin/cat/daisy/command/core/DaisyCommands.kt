@file:Suppress("unused")

package cat.daisy.command.core

import cat.daisy.command.DaisyCommandAvailabilityContext
import cat.daisy.command.dsl.CommandSetBuilder
import io.papermc.paper.command.brigadier.BasicCommand
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

fun JavaPlugin.registerCommands(vararg commands: CommandSpec) {
    registerCompiled(commands.toList(), DaisyConfig())
}

fun JavaPlugin.registerCommands(block: CommandSetBuilder.() -> Unit) {
    val built = CommandSetBuilder().apply(block).build()
    registerCompiled(built.commands, built.config)
}

private fun JavaPlugin.registerCompiled(
    commands: List<CommandSpec>,
    config: DaisyConfig,
) {
    val activeCommands = CommandAvailabilityFilter.filter(commands, DaisyCommandAvailabilityContext(this))
    validateRootKeys(activeCommands)
    val runtime = CommandRuntime(logger = logger, config = config)
    for (command in activeCommands) {
        val compiled = command.compiled
        registerCommand(command.name, command.description, command.aliases, PaperCommandAdapter(compiled, runtime))
    }
}

private fun validateRootKeys(commands: List<CommandSpec>) {
    val keys = LinkedHashMap<String, String>()
    for (command in commands) {
        registerRootKey(keys, command.name, command.name)
        for (alias in command.aliases) {
            registerRootKey(keys, alias, command.name)
        }
    }
}

internal object CommandAvailabilityFilter {
    fun filter(
        commands: List<CommandSpec>,
        context: DaisyCommandAvailabilityContext,
    ): List<CommandSpec> = commands.mapNotNull { filterCommand(it, context) }

    private fun filterCommand(
        command: CommandSpec,
        context: DaisyCommandAvailabilityContext,
    ): CommandSpec? {
        if (!command.availability(context)) {
            return null
        }

        val filteredChildren = command.children.mapNotNull { filterNode(it, context) }
        if (command.handler == null && filteredChildren.isEmpty()) {
            return null
        }

        return CommandSpec(
            name = command.name,
            description = command.description,
            aliases = command.aliases,
            availability = command.availability,
            permission = command.permission,
            senderConstraint = command.senderConstraint,
            cooldown = command.cooldown,
            arguments = command.arguments,
            requirements = command.requirements,
            children = filteredChildren,
            handler = command.handler,
        )
    }

    private fun filterNode(
        node: CommandNodeSpec,
        context: DaisyCommandAvailabilityContext,
    ): CommandNodeSpec? {
        if (!node.availability(context)) {
            return null
        }

        val filteredChildren = node.children.mapNotNull { filterNode(it, context) }
        if (node.handler == null && filteredChildren.isEmpty()) {
            return null
        }

        return node.copy(children = filteredChildren)
    }
}

private fun registerRootKey(
    keys: MutableMap<String, String>,
    rawKey: String,
    owner: String,
) {
    val normalized = rawKey.lowercase()
    require(keys.putIfAbsent(normalized, owner) == null) {
        "Duplicate root command key '$rawKey'."
    }
}

internal class PaperCommandAdapter(
    private val command: CompiledCommand,
    private val runtime: CommandRuntime,
) : BasicCommand {
    override fun execute(
        commandSourceStack: CommandSourceStack,
        args: Array<String>,
    ) {
        command.execute(commandSourceStack.sender, command.name, args.toList(), runtime)
    }

    override fun suggest(
        commandSourceStack: CommandSourceStack,
        args: Array<String>,
    ): Collection<String> = command.suggest(commandSourceStack.sender, args.toList(), runtime)

    override fun canUse(sender: CommandSender): Boolean =
        when (command.root.senderConstraint) {
            SenderConstraint.ANY -> {
                command.root.ownPermission == null || sender.hasPermission(command.root.ownPermission)
            }

            SenderConstraint.PLAYER_ONLY -> {
                sender is org.bukkit.entity.Player &&
                    (command.root.ownPermission == null || sender.hasPermission(command.root.ownPermission))
            }

            SenderConstraint.CONSOLE_ONLY -> {
                sender is org.bukkit.command.ConsoleCommandSender &&
                    (command.root.ownPermission == null || sender.hasPermission(command.root.ownPermission))
            }
        }

    override fun permission(): String? = command.root.ownPermission
}
