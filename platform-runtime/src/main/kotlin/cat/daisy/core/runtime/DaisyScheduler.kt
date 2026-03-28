package cat.daisy.core.runtime

import cat.daisy.core.DaisyHandle
import java.time.Duration

public interface DaisyScheduler {
    public fun run(task: () -> Unit): DaisyHandle

    public fun later(
        delay: Duration,
        task: () -> Unit,
    ): DaisyHandle

    public fun repeating(
        period: Duration,
        task: () -> Unit,
    ): DaisyHandle

    public fun main(task: () -> Unit): DaisyHandle
}
