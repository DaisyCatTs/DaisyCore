@file:Suppress("unused")

package cat.daisy.command.context

import cat.daisy.command.arguments.ArgumentRef
import cat.daisy.command.core.CommandRuntime
import cat.daisy.command.text.DaisyText.mm
import cat.daisy.text.DaisyMessages
import cat.daisy.text.withPlaceholders
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.entity.Player

internal class ResolvedArguments(
    private val valuesBySlot: Array<Any?>,
    private val valuesByName: Map<String, Any?>,
) {
    @Suppress("UNCHECKED_CAST")
    fun <T> get(ref: ArgumentRef<T>): T = valuesBySlot[ref.definition.slot] as T

    fun byName(name: String): Any? = valuesByName[name]
}

internal object AbortExecution : RuntimeException(null, null, false, false)

open class CommandContext internal constructor(
    val sender: CommandSender,
    val label: String,
    val path: List<String>,
    val args: List<String>,
    private val resolvedArguments: ResolvedArguments,
    private val runtime: CommandRuntime,
) {
    private var successful = true

    val isPlayer: Boolean
        get() = sender is Player

    val isConsole: Boolean
        get() = sender is ConsoleCommandSender

    open val player: Player
        get() = sender as? Player ?: error("This command is not executing as a player.")

    open val console: ConsoleCommandSender
        get() = sender as? ConsoleCommandSender ?: error("This command is not executing from the console.")

    internal fun wasSuccessful(): Boolean = successful

    fun reply(message: String) {
        sendWithPrefix(message)
    }

    fun replyMm(message: String) {
        sender.sendMessage(message.mm())
    }

    fun reply(component: Component) {
        sender.sendMessage(component)
    }

    fun fail(message: String): Nothing {
        successful = false
        sendWithPrefix(message)
        throw AbortExecution
    }

    fun failLang(
        key: String,
        vararg placeholders: Pair<String, Any?>,
    ): Nothing = fail(lang(key, *placeholders))

    fun <T> get(ref: ArgumentRef<T>): T = resolvedArguments.get(ref)

    @Suppress("UNCHECKED_CAST")
    fun <T> arg(name: String): T =
        resolvedArguments.byName(name) as? T
            ?: error("Argument '$name' is missing or has the wrong type.")

    @Suppress("UNCHECKED_CAST")
    fun <T> argOr(
        name: String,
        default: T,
    ): T = resolvedArguments.byName(name) as? T ?: default

    fun string(name: String): String = arg(name)

    fun stringOr(
        name: String,
        default: String,
    ): String = argOr(name, default)

    fun int(name: String): Int = arg(name)

    fun intOr(
        name: String,
        default: Int,
    ): Int = argOr(name, default)

    fun double(name: String): Double = arg(name)

    fun doubleOr(
        name: String,
        default: Double,
    ): Double = argOr(name, default)

    fun lang(
        key: String,
        vararg placeholders: Pair<String, Any?>,
    ): String = DaisyMessages.resolve(key)?.withPlaceholders(*placeholders) ?: key

    fun langList(key: String): List<Component> = DaisyMessages.resolveList(key).map { it.mm() }

    fun replyLang(
        key: String,
        vararg placeholders: Pair<String, Any?>,
    ) {
        reply(lang(key, *placeholders))
    }

    fun loading(key: String = "loading") {
        replyLang(key)
    }

    operator fun <T> ArgumentRef<T>.invoke(): T = get(this)

    internal fun sendFramework(message: String) {
        successful = false
        sendWithPrefix(message)
    }

    internal fun logFailure(
        message: String,
        throwable: Throwable,
    ) {
        runtime.logger.severe(message)
        runtime.logger.severe(throwable.stackTraceToString())
    }

    private fun sendWithPrefix(message: String) {
        sender.sendMessage((runtime.config.messages.prefix + message).mm())
    }
}

class PlayerCommandContext internal constructor(
    override val player: Player,
    label: String,
    path: List<String>,
    args: List<String>,
    resolvedArguments: ResolvedArguments,
    runtime: CommandRuntime,
) : CommandContext(player, label, path, args, resolvedArguments, runtime)

class ConsoleCommandContext internal constructor(
    override val console: ConsoleCommandSender,
    label: String,
    path: List<String>,
    args: List<String>,
    resolvedArguments: ResolvedArguments,
    runtime: CommandRuntime,
) : CommandContext(console, label, path, args, resolvedArguments, runtime)
