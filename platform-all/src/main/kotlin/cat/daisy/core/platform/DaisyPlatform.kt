package cat.daisy.core.platform

import cat.daisy.core.DaisyFeature
import cat.daisy.core.DaisyHandle
import org.bukkit.plugin.Plugin

public class DaisyPlatform internal constructor(
    public val plugin: Plugin,
    public val features: Set<DaisyFeature>,
) : DaisyHandle {
    override fun close() {
        // Runtime shutdown hooks will be attached as feature modules are implemented.
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

    public fun text() {
        features += DaisyFeature.TEXT
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

    internal fun build(): DaisyPlatform = DaisyPlatform(plugin = plugin, features = features.toSet())
}
