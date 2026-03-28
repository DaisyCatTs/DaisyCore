@file:Suppress("unused")

package cat.daisy.command

import cat.daisy.command.context.CommandContext
import cat.daisy.command.context.ConsoleCommandContext
import cat.daisy.command.context.PlayerCommandContext
import cat.daisy.command.core.CommandSpec
import cat.daisy.command.dsl.CommandSetBuilder
import cat.daisy.command.dsl.CommandBuilder
import cat.daisy.command.dsl.command
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CompletionStage

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

public abstract class DaisyCommandGroup(
    block: CommandSetBuilder.() -> Unit,
) : DaisyCommandProvider {
    private val built = CommandSetBuilder().apply(block).build()

    final override fun commands(): List<DaisyCommand> = built.commands
}

public fun commandGroup(block: CommandSetBuilder.() -> Unit): DaisyCommandProvider =
    object : DaisyCommandProvider {
        private val built = CommandSetBuilder().apply(block).build()

        override fun commands(): List<DaisyCommand> = built.commands
    }

public fun <T> CompletionStage<Result<T>>.replyResult(
    context: DaisyCommandContext,
    success: DaisyCommandContext.(T) -> Unit,
    failure: DaisyCommandContext.(Throwable) -> Unit,
) {
    whenComplete { result, throwable ->
        when {
            throwable != null -> context.failure(throwable)
            result == null -> context.failure(IllegalStateException("Async command result was null."))
            else ->
                result.fold(
                    onSuccess = { value -> context.success(value) },
                    onFailure = { error -> context.failure(error) },
                )
        }
    }
}
