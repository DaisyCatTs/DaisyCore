package cat.daisy.tablist

import cat.daisy.core.DaisyAudienceContext
import cat.daisy.core.DaisyHandle
import cat.daisy.core.runtime.DaisyRuntime
import cat.daisy.placeholder.DaisyPlaceholderRegistry
import cat.daisy.text.DaisyTextRenderer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.time.Duration
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

public data class DaisyTablistOptions(
    val updateInterval: Duration = Duration.ofSeconds(1),
    val autoRefresh: Boolean = true,
)

public data class DaisyTablist(
    val header: DaisyTextRenderer<DaisyTablistRenderContext>? = null,
    val footer: DaisyTextRenderer<DaisyTablistRenderContext>? = null,
    val options: DaisyTablistOptions = DaisyTablistOptions(),
)

public data class DaisyTablistRenderContext(
    val player: Player,
    val audience: DaisyAudienceContext,
    val placeholders: DaisyPlaceholderRegistry,
)

public interface DaisyTablistSession : AutoCloseable {
    public val player: Player
    public val tablist: DaisyTablist

    public fun invalidate()

    public fun refreshNow()
}

public interface DaisyTablistPlatform : AutoCloseable {
    public fun show(player: Player, tablist: DaisyTablist): DaisyTablistSession

    public fun close(player: Player)

    public fun session(player: Player): DaisyTablistSession?
}

public class DaisyTablistOptionsBuilder internal constructor(
    private var current: DaisyTablistOptions,
) {
    public var updateInterval: Duration
        get() = current.updateInterval
        set(value) {
            current = current.copy(updateInterval = value)
        }

    public var autoRefresh: Boolean
        get() = current.autoRefresh
        set(value) {
            current = current.copy(autoRefresh = value)
        }

    internal fun build(): DaisyTablistOptions = current
}

public class DaisyTablistBuilder {
    private var header: DaisyTextRenderer<DaisyTablistRenderContext>? = null
    private var footer: DaisyTextRenderer<DaisyTablistRenderContext>? = null
    private var options = DaisyTablistOptions()

    public fun header(renderer: DaisyTextRenderer<DaisyTablistRenderContext>) {
        header = renderer
    }

    public fun footer(renderer: DaisyTextRenderer<DaisyTablistRenderContext>) {
        footer = renderer
    }

    public fun options(block: DaisyTablistOptionsBuilder.() -> Unit) {
        options = DaisyTablistOptionsBuilder(options).apply(block).build()
    }

    internal fun build(): DaisyTablist = DaisyTablist(header = header, footer = footer, options = options)
}

public fun tablist(block: DaisyTablistBuilder.() -> Unit): DaisyTablist = DaisyTablistBuilder().apply(block).build()

public class DaisyTablistPlatformImpl(
    private val plugin: Plugin,
    private val runtime: DaisyRuntime,
    private val placeholders: DaisyPlaceholderRegistry,
) : DaisyTablistPlatform {
    private val sessions = ConcurrentHashMap<UUID, TablistSessionImpl>()
    private val listener =
        object : Listener {
            @EventHandler
            fun onQuit(event: PlayerQuitEvent) {
                close(event.player)
            }
        }

    init {
        Bukkit.getPluginManager().registerEvents(listener, plugin)
    }

    override fun show(player: Player, tablist: DaisyTablist): DaisyTablistSession {
        close(player)
        return TablistSessionImpl(player, tablist, runtime, placeholders).also {
            sessions[player.uniqueId] = it
        }
    }

    override fun close(player: Player) {
        sessions.remove(player.uniqueId)?.close()
    }

    override fun session(player: Player): DaisyTablistSession? = sessions[player.uniqueId]

    override fun close() {
        val snapshot = sessions.values.toList()
        sessions.clear()
        snapshot.forEach(TablistSessionImpl::close)
        PlayerQuitEvent.getHandlerList().unregister(listener)
    }
}

private class TablistSessionImpl(
    override val player: Player,
    override val tablist: DaisyTablist,
    private val runtime: DaisyRuntime,
    private val placeholders: DaisyPlaceholderRegistry,
) : DaisyTablistSession {
    private var refreshHandle: DaisyHandle? = null
    private var closed = false

    init {
        render()
        if (tablist.options.autoRefresh) {
            refreshHandle = runtime.scheduler.repeating(tablist.options.updateInterval) { refreshNow() }
        }
    }

    override fun invalidate() {
        refreshNow()
    }

    override fun refreshNow() {
        if (closed) {
            return
        }
        runtime.scheduler.main { render() }
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        refreshHandle?.close()
        refreshHandle = null
        if (player.isOnline) {
            player.sendPlayerListHeaderAndFooter(Component.empty(), Component.empty())
        }
    }

    private fun render() {
        if (closed || !player.isOnline) {
            return
        }
        val context =
            DaisyTablistRenderContext(
                player = player,
                audience = DaisyAudienceContext(player.uniqueId, Locale.ENGLISH, player.world.name),
                placeholders = placeholders,
            )
        player.sendPlayerListHeaderAndFooter(
            tablist.header?.render(context) ?: Component.empty(),
            tablist.footer?.render(context) ?: Component.empty(),
        )
    }
}
