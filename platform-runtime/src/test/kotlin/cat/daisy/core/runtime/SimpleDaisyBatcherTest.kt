package cat.daisy.core.runtime

import kotlin.test.Test
import kotlin.test.assertEquals

class SimpleDaisyBatcherTest {
    @Test
    fun `latest action wins per key`() {
        val batcher = SimpleDaisyBatcher()
        var result = ""

        batcher.submit("score") { result = "one" }
        batcher.submit("score") { result = "two" }
        batcher.flush()

        assertEquals("two", result)
    }
}
