package cat.daisy.tablist

import cat.daisy.text.DaisyText
import org.junit.jupiter.api.Test
import java.time.Duration
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DaisyTablistTest {
    @Test
    fun `header and footer are optional`() {
        val built = tablist {}

        assertNull(built.header)
        assertNull(built.footer)
    }

    @Test
    fun `options builder updates defaults`() {
        val built =
            tablist {
                header { DaisyText.plain("Welcome") }
                options {
                    updateInterval = Duration.ofSeconds(2)
                    autoRefresh = false
                }
            }

        assertNotNull(built.header)
        assertEquals(Duration.ofSeconds(2), built.options.updateInterval)
        assertEquals(false, built.options.autoRefresh)
    }
}
