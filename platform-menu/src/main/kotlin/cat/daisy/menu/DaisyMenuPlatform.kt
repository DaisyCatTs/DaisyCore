@file:Suppress("unused")

package cat.daisy.menu

import org.bukkit.plugin.Plugin

public interface DaisyMenuPlatform : AutoCloseable {
    public val plugin: Plugin
    public fun openMenuCount(): Int
}

public class DaisyMenuPlatformImpl(
    override val plugin: Plugin,
) : DaisyMenuPlatform {
    init {
        if (!DaisyMenuRuntime.isInitialized()) {
            DaisyMenuRuntime.initialize(plugin)
        }
    }

    override fun openMenuCount(): Int = DaisyMenuRuntime.getOpenMenuCount()

    override fun close() {
        if (DaisyMenuRuntime.isInitialized()) {
            DaisyMenuRuntime.shutdown()
        }
    }
}
