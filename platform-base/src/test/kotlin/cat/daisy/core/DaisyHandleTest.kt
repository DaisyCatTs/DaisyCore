package cat.daisy.core

import kotlin.test.Test
import kotlin.test.assertTrue

class DaisyHandleTest {
    @Test
    fun `close executes handle body`() {
        var closed = false
        val handle = DaisyHandle { closed = true }

        handle.close()

        assertTrue(closed)
    }
}
