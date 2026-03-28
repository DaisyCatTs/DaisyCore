@file:Suppress("unused")

package cat.daisy.command

import cat.daisy.command.core.registerCommands
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.jar.JarFile

public interface DaisyCommandRegistry : AutoCloseable {
    public val commands: List<DaisyCommand>

    public fun reload()
}

public class AutoLoadingDaisyCommandRegistry internal constructor(
    private val plugin: JavaPlugin,
) : DaisyCommandRegistry {
    private var loadedCommands: List<DaisyCommand> = emptyList()

    override val commands: List<DaisyCommand>
        get() = loadedCommands

    override fun reload() {
        loadedCommands = DaisyCommandDiscovery.discover(plugin).sortedBy(DaisyCommand::name)
        if (loadedCommands.isNotEmpty()) {
            plugin.registerCommands(*loadedCommands.toTypedArray())
        }
    }

    override fun close() {
        loadedCommands = emptyList()
    }
}

public fun JavaPlugin.autoLoadDaisyCommands(): DaisyCommandRegistry =
    AutoLoadingDaisyCommandRegistry(this).also(DaisyCommandRegistry::reload)

internal object DaisyCommandDiscovery {
    fun discover(plugin: JavaPlugin): List<DaisyCommand> {
        val source = plugin.javaClass.protectionDomain.codeSource?.location ?: return emptyList()
        val jarFile = File(source.toURI())
        if (!jarFile.isFile || !jarFile.name.endsWith(".jar", ignoreCase = true)) {
            return emptyList()
        }

        val providers = mutableListOf<DaisyCommandProvider>()
        JarFile(jarFile).use { jar ->
            val classNames =
                jar.entries()
                    .asSequence()
                    .map { it.name }
                    .filter { it.endsWith(".class") }
                    .filterNot { it.contains('$') }
                    .map { it.removeSuffix(".class").replace('/', '.') }
                    .sorted()
                    .toList()

            for (className in classNames) {
                val clazz =
                    runCatching { Class.forName(className, false, plugin.javaClass.classLoader) }
                        .getOrNull()
                        ?: continue
                if (!clazz.isAnnotationPresent(DaisyCommandSet::class.java)) {
                    continue
                }
                if (!DaisyCommandProvider::class.java.isAssignableFrom(clazz)) {
                    plugin.logger.warning("Ignoring @DaisyCommandSet on $className because it does not implement DaisyCommandProvider.")
                    continue
                }

                val provider =
                    runCatching {
                        @Suppress("UNCHECKED_CAST")
                        when {
                            runCatching { clazz.getField("INSTANCE").get(null) }.getOrNull() is DaisyCommandProvider ->
                                clazz.getField("INSTANCE").get(null) as DaisyCommandProvider
                            else -> clazz.getDeclaredConstructor().apply { isAccessible = true }.newInstance() as DaisyCommandProvider
                        }
                    }.getOrElse { throwable ->
                        plugin.logger.warning("Failed to load Daisy command provider $className: ${throwable.message}")
                        return@getOrElse null
                    } ?: continue

                providers += provider
            }
        }

        return providers.flatMap(DaisyCommandProvider::commands)
    }
}
