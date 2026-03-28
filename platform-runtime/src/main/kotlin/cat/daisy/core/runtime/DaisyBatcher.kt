package cat.daisy.core.runtime

public interface DaisyBatcher {
    public fun submit(key: String, action: () -> Unit)

    public fun flush()
}
