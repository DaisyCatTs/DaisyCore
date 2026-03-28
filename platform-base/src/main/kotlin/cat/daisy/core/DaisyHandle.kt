package cat.daisy.core

/**
 * Represents a runtime resource that must be closed deterministically.
 */
fun interface DaisyHandle : AutoCloseable {
    override fun close()
}
