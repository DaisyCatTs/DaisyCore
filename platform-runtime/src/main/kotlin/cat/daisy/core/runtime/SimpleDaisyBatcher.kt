package cat.daisy.core.runtime

import java.util.LinkedHashMap

internal class SimpleDaisyBatcher : DaisyBatcher {
    private val pending = LinkedHashMap<String, () -> Unit>()

    override fun submit(
        key: String,
        action: () -> Unit,
    ) {
        pending[key] = action
    }

    override fun flush() {
        val snapshot = pending.values.toList()
        pending.clear()
        snapshot.forEach { it() }
    }
}
