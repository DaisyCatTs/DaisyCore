package cat.daisy.core.runtime

import cat.daisy.core.DaisyHandle
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.time.Duration
import java.util.LinkedHashSet
import java.util.logging.Logger

public class PaperDaisyRuntime(
    private val plugin: Plugin,
) : DaisyRuntime {
    private val handles = LinkedHashSet<DaisyHandle>()

    override val scheduler: DaisyScheduler = PaperDaisyScheduler(plugin)
    override val batcher: DaisyBatcher = SimpleDaisyBatcher()
    override val logger: Logger = plugin.logger

    override fun register(handle: DaisyHandle): DaisyHandle {
        handles += handle
        return DaisyHandle {
            runCatching(handle::close)
            handles -= handle
        }
    }

    public fun close() {
        val snapshot = handles.toList()
        handles.clear()
        snapshot.asReversed().forEach {
            runCatching(it::close)
        }
    }
}

private class PaperDaisyScheduler(
    private val plugin: Plugin,
) : DaisyScheduler {
    override fun run(task: () -> Unit): DaisyHandle =
        DaisyHandle {
            Bukkit.getScheduler().runTask(plugin, Runnable(task))
        }

    override fun later(
        delay: Duration,
        task: () -> Unit,
    ): DaisyHandle {
        val scheduled =
            Bukkit.getScheduler().runTaskLater(
                plugin,
                Runnable(task),
                delay.toTicks(),
            )
        return DaisyHandle { scheduled.cancel() }
    }

    override fun repeating(
        period: Duration,
        task: () -> Unit,
    ): DaisyHandle {
        val scheduled =
            Bukkit.getScheduler().runTaskTimer(
                plugin,
                Runnable(task),
                period.toTicks(),
                period.toTicks(),
            )
        return DaisyHandle { scheduled.cancel() }
    }

    override fun main(task: () -> Unit): DaisyHandle {
        if (Bukkit.isPrimaryThread()) {
            task()
            return DaisyHandle { }
        }
        val scheduled = Bukkit.getScheduler().runTask(plugin, Runnable(task))
        return DaisyHandle { scheduled.cancel() }
    }
}

private fun Duration.toTicks(): Long = (toMillis() / 50L).coerceAtLeast(1L)
