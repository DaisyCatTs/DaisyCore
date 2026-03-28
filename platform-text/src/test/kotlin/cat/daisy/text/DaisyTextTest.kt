package cat.daisy.text

import kotlin.test.Test
import kotlin.test.assertEquals
import net.kyori.adventure.text.Component

class DaisyTextTest {
    @Test
    fun `plain helper returns plain component`() {
        val component = DaisyText.plain("Daisy")

        assertEquals(Component.text("Daisy"), component)
    }
}
