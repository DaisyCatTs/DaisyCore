package cat.daisy.core.runtime

import java.util.logging.Logger

public interface DaisyRuntime {
    public val scheduler: DaisyScheduler
    public val batcher: DaisyBatcher
    public val logger: Logger
    public fun register(handle: cat.daisy.core.DaisyHandle): cat.daisy.core.DaisyHandle
}
