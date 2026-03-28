package cat.daisy.scoreboard

import cat.daisy.text.DaisyText
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DaisySidebarTest {
    @Test
    fun `duplicate keys are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            sidebar {
                line("coins") { DaisyText.plain("1") }
                line("coins") { DaisyText.plain("2") }
            }
        }
    }

    @Test
    fun `blank lines receive unique keys`() {
        val built =
            sidebar {
                blank()
                blank()
            }

        assertEquals(2, built.lines.size)
        assertEquals(2, built.lines.map(DaisySidebarLine::key).distinct().size)
    }

    @Test
    fun `sidebar line count is capped to transport limit`() {
        assertFailsWith<IllegalArgumentException> {
            sidebar {
                repeat(16) { index ->
                    line("line$index") { DaisyText.plain(index.toString()) }
                }
            }
        }
    }
}
