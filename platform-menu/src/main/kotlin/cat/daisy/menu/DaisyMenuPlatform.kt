@file:Suppress("unused")

package cat.daisy.menu

import org.bukkit.plugin.Plugin

public interface DaisyMenuPlatform : AutoCloseable {
    public val plugin: Plugin
    public fun openMenuCount(): Int
}

internal class DaisyMenuPlatformImpl(
    override val plugin: Plugin,
) : DaisyMenuPlatform {
    init {
        if (!DaisyMenu.isInitialized()) {
            DaisyMenu.initialize(plugin)
        }
    }

    override fun openMenuCount(): Int = DaisyMenu.getOpenMenuCount()

    override fun close() {
        if (DaisyMenu.isInitialized()) {
            DaisyMenu.shutdown()
        }
    }
}

public typealias DaisyMenuDefinition = Menu
public typealias DaisyMenuBuilderDsl = MenuBuilder
public typealias DaisyMenuSession = MenuSession
