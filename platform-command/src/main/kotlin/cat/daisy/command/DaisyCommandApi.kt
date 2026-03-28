@file:Suppress("unused")

package cat.daisy.command

import cat.daisy.command.context.CommandContext
import cat.daisy.command.context.ConsoleCommandContext
import cat.daisy.command.context.PlayerCommandContext
import cat.daisy.command.core.CommandSpec
import cat.daisy.command.dsl.CommandBuilder
import cat.daisy.command.dsl.command
import org.bukkit.plugin.java.JavaPlugin

public typealias DaisyCommand = CommandSpec
public typealias DaisySubcommand = CommandSpec
public typealias DaisyCommandContext = CommandContext
public typealias DaisyPlayerCommandContext = PlayerCommandContext
public typealias DaisyConsoleCommandContext = ConsoleCommandContext
public typealias DaisyCommandBuilder = CommandBuilder

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
public annotation class DaisyCommandSet

public interface DaisyCommandProvider {
    public fun commands(): List<DaisyCommand>
}

public data class DaisyCommandAvailabilityContext(
    val plugin: JavaPlugin,
)

public fun daisyCommand(
    name: String,
    block: DaisyCommandBuilder.() -> Unit,
): DaisyCommand = command(name, block)
