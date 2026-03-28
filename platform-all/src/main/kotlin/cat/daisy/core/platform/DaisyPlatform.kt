package cat.daisy.core.platform

import cat.daisy.command.DaisyCommandRegistry
import cat.daisy.command.autoLoadDaisyCommands
import cat.daisy.core.DaisyFeature
import cat.daisy.core.DaisyHandle
import cat.daisy.core.runtime.DaisyRuntime
import cat.daisy.core.runtime.PaperDaisyRuntime
import cat.daisy.menu.DaisyMenuPlatform
import cat.daisy.menu.DaisyMenuPlatformImpl
import cat.daisy.placeholder.DaisyPlaceholderRegistry
import cat.daisy.scoreboard.DaisyScoreboardPlatform
import cat.daisy.scoreboard.DaisyScoreboardPlatformImpl
import cat.daisy.tablist.DaisyTablistPlatform
import cat.daisy.tablist.DaisyTablistPlatformImpl
import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyTextSource
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

public class DaisyPlatform internal constructor(
    public val plugin: Plugin,
    public val features: Set<DaisyFeature>,
    public val runtime: DaisyRuntime,
    public val placeholders: DaisyPlaceholderRegistry?,
    public val commands: DaisyCommandRegistry?,
    public val menus: DaisyMenuPlatform?,
    public val scoreboards: DaisyScoreboardPlatform?,
    public val tablists: DaisyTablistPlatform?,
    public val messageSource: DaisyTextSource?,
) : DaisyHandle {
    override fun close() {
        tablists?.close()
        scoreboards?.close()
        menus?.close()
        commands?.close()
        messageSource?.let { DaisyMessages.clear(plugin) }
        (runtime as? PaperDaisyRuntime)?.close()
    }

    public companion object {
        public fun create(
            plugin: Plugin,
            block: DaisyPlatformBuilder.() -> Unit = {},
        ): DaisyPlatform = DaisyPlatformBuilder(plugin).apply(block).build()
    }
}

public class DaisyPlatformBuilder internal constructor(
    private val plugin: Plugin,
) {
    private val features = linkedSetOf<DaisyFeature>()
    private var messageSource: DaisyTextSource? = null

    public fun text() {
        features += DaisyFeature.TEXT
    }

    public fun messages(source: DaisyTextSource) {
        messageSource = source
        text()
    }

    public fun placeholders() {
        features += DaisyFeature.PLACEHOLDERS
    }

    public fun items() {
        features += DaisyFeature.ITEMS
    }

    public fun commands() {
        features += DaisyFeature.COMMANDS
    }

    public fun menus() {
        features += DaisyFeature.MENUS
    }

    public fun scoreboards() {
        features += DaisyFeature.SCOREBOARDS
    }

    public fun tablists() {
        features += DaisyFeature.TABLISTS
    }

    internal fun build(): DaisyPlatform {
        val runtime = PaperDaisyRuntime(plugin)
        messageSource?.let { DaisyMessages.install(plugin, it) }
        val placeholderRegistry =
            if (
                DaisyFeature.PLACEHOLDERS in features ||
                DaisyFeature.SCOREBOARDS in features ||
                DaisyFeature.TABLISTS in features
            ) {
                DaisyPlaceholderRegistry()
            } else {
                null
            }

        val scoreboardPlaceholders = placeholderRegistry ?: DaisyPlaceholderRegistry()
        val tablistPlaceholders = placeholderRegistry ?: DaisyPlaceholderRegistry()

        val scoreboards =
            if (DaisyFeature.SCOREBOARDS in features) {
                DaisyScoreboardPlatformImpl(plugin, runtime, scoreboardPlaceholders)
            } else {
                null
            }
        val tablists =
            if (DaisyFeature.TABLISTS in features) {
                DaisyTablistPlatformImpl(plugin, runtime, tablistPlaceholders)
            } else {
                null
            }
        val menus =
            if (DaisyFeature.MENUS in features) {
                DaisyMenuPlatformImpl(plugin)
            } else {
                null
            }

        val commands =
            if (DaisyFeature.COMMANDS in features && plugin is JavaPlugin) {
                plugin.autoLoadDaisyCommands()
            } else {
                null
            }

        return DaisyPlatform(
            plugin = plugin,
            features = features.toSet(),
            runtime = runtime,
            placeholders = placeholderRegistry,
            commands = commands,
            menus = menus,
            scoreboards = scoreboards,
            tablists = tablists,
            messageSource = messageSource,
        )
    }
}
