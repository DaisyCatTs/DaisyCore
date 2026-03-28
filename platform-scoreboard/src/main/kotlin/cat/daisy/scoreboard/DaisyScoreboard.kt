package cat.daisy.scoreboard

import cat.daisy.core.DaisyAudienceContext
import cat.daisy.core.DaisyHandle
import cat.daisy.core.runtime.DaisyRuntime
import cat.daisy.placeholder.DaisyPlaceholderRegistry
import cat.daisy.text.DaisyText
import cat.daisy.text.DaisyTextRenderer
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.time.Duration
import java.util.Locale
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

private val SIDEBAR_ENTRIES: List<String> =
    (0..14).map { "\u00A7${it.toString(16)}" }

public data class DaisySidebarLine(
    val key: String,
    val renderer: DaisyTextRenderer<DaisySidebarRenderContext>,
    val visible: (DaisySidebarRenderContext) -> Boolean = { true },
)

public data class DaisySidebarOptions(
    val updateInterval: Duration = Duration.ofSeconds(1),
    val autoRefresh: Boolean = true,
)

public data class DaisySidebar(
    val title: DaisyTextRenderer<DaisySidebarRenderContext>,
    val lines: List<DaisySidebarLine>,
    val options: DaisySidebarOptions = DaisySidebarOptions(),
)

public data class DaisySidebarRenderContext(
    val player: Player,
    val audience: DaisyAudienceContext,
    val placeholders: DaisyPlaceholderRegistry,
)

public interface DaisySidebarSession : AutoCloseable {
    public val player: Player
    public val sidebar: DaisySidebar

    public fun invalidate()

    public fun invalidate(vararg keys: String)

    public fun refreshNow()
}

public interface DaisyScoreboardPlatform : AutoCloseable {
    public fun show(player: Player, sidebar: DaisySidebar): DaisySidebarSession

    public fun close(player: Player)

    public fun session(player: Player): DaisySidebarSession?
}

public class DaisySidebarOptionsBuilder internal constructor(
    private var current: DaisySidebarOptions,
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

    internal fun build(): DaisySidebarOptions = current
}

public class DaisySidebarBuilder {
    private var titleRenderer: DaisyTextRenderer<DaisySidebarRenderContext> = DaisyTextRenderer { DaisyText.plain("Daisy") }
    private val lines = mutableListOf<DaisySidebarLine>()
    private var options = DaisySidebarOptions()
    private var blankCounter = 0

    public fun title(component: Component) {
        titleRenderer = DaisyTextRenderer { component }
    }

    public fun title(renderer: DaisyTextRenderer<DaisySidebarRenderContext>) {
        titleRenderer = renderer
    }

    public fun line(
        key: String,
        renderer: DaisyTextRenderer<DaisySidebarRenderContext>,
    ) {
        require(lines.none { it.key == key }) { "Duplicate sidebar key '$key'." }
        lines += DaisySidebarLine(key, renderer)
    }

    public fun lineIf(
        key: String,
        visible: (DaisySidebarRenderContext) -> Boolean,
        renderer: DaisyTextRenderer<DaisySidebarRenderContext>,
    ) {
        require(lines.none { it.key == key }) { "Duplicate sidebar key '$key'." }
        lines += DaisySidebarLine(key, renderer, visible)
    }

    public fun blank(key: String = "__blank_${blankCounter++}") {
        line(key, DaisyTextRenderer { DaisyText.plain(" ") })
    }

    public fun options(block: DaisySidebarOptionsBuilder.() -> Unit) {
        options = DaisySidebarOptionsBuilder(options).apply(block).build()
    }

    internal fun build(): DaisySidebar {
        require(lines.size <= SIDEBAR_ENTRIES.size) { "DaisySidebar currently supports up to ${SIDEBAR_ENTRIES.size} lines." }
        return DaisySidebar(title = titleRenderer, lines = lines.toList(), options = options)
    }
}

public fun sidebar(block: DaisySidebarBuilder.() -> Unit): DaisySidebar = DaisySidebarBuilder().apply(block).build()

public class DaisyScoreboardPlatformImpl(
    private val plugin: Plugin,
    private val runtime: DaisyRuntime,
    private val placeholders: DaisyPlaceholderRegistry,
) : DaisyScoreboardPlatform {
    private val sessions = ConcurrentHashMap<UUID, SidebarSessionImpl>()
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

    override fun show(player: Player, sidebar: DaisySidebar): DaisySidebarSession {
        close(player)
        return SidebarSessionImpl(player, sidebar, runtime, placeholders).also {
            sessions[player.uniqueId] = it
        }
    }

    override fun close(player: Player) {
        sessions.remove(player.uniqueId)?.close()
    }

    override fun session(player: Player): DaisySidebarSession? = sessions[player.uniqueId]

    override fun close() {
        val snapshot = sessions.values.toList()
        sessions.clear()
        snapshot.forEach(SidebarSessionImpl::close)
        PlayerQuitEvent.getHandlerList().unregister(listener)
    }
}

private class SidebarSessionImpl(
    override val player: Player,
    override val sidebar: DaisySidebar,
    private val runtime: DaisyRuntime,
    private val placeholders: DaisyPlaceholderRegistry,
) : DaisySidebarSession {
    private val previousScoreboard = player.scoreboard
    private val scoreboard: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val objective: Objective = scoreboard.registerNewObjective("daisy", Criteria.DUMMY, sidebar.title.render(context()))
    private var refreshHandle: DaisyHandle? = null
    private var closed = false

    init {
        objective.displaySlot = DisplaySlot.SIDEBAR
        player.scoreboard = scoreboard
        renderAll()
        if (sidebar.options.autoRefresh) {
            refreshHandle = runtime.scheduler.repeating(sidebar.options.updateInterval) { refreshNow() }
        }
    }

    override fun invalidate() {
        refreshNow()
    }

    override fun invalidate(vararg keys: String) {
        if (closed) {
            return
        }
        val requested = keys.toSet()
        runtime.scheduler.main {
            renderVisibleLines()
                .filter { it.key in requested }
                .forEachIndexed(::renderLine)
        }
    }

    override fun refreshNow() {
        if (closed) {
            return
        }
        runtime.scheduler.main { renderAll() }
    }

    override fun close() {
        if (closed) {
            return
        }
        closed = true
        refreshHandle?.close()
        refreshHandle = null
        scoreboard.teams.toList().forEach(Team::unregister)
        scoreboard.objectives.toList().forEach(Objective::unregister)
        if (player.isOnline) {
            player.scoreboard = previousScoreboard
        }
    }

    private fun renderAll() {
        if (closed) {
            return
        }
        objective.displayName(sidebar.title.render(context()))
        scoreboard.teams.toList().forEach(Team::unregister)
        renderVisibleLines().forEachIndexed(::renderLine)
    }

    private fun renderLine(
        index: Int,
        line: DaisySidebarLine,
    ) {
        val entry = SIDEBAR_ENTRIES[index]
        val team = scoreboard.getTeam(line.key) ?: scoreboard.registerNewTeam(line.key)
        team.entries.forEach(team::removeEntry)
        team.addEntry(entry)
        team.prefix(line.renderer.render(context()))
        objective.getScore(entry).score = SIDEBAR_ENTRIES.size - index
    }

    private fun renderVisibleLines(): List<DaisySidebarLine> = sidebar.lines.filter { it.visible(context()) }

    private fun context(): DaisySidebarRenderContext =
        DaisySidebarRenderContext(
            player = player,
            audience = DaisyAudienceContext(player.uniqueId, Locale.ENGLISH, player.world.name),
            placeholders = placeholders,
        )
}
