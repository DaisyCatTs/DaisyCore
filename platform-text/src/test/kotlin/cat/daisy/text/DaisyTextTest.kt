package cat.daisy.text

import net.kyori.adventure.text.Component
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DaisyTextTest {
    @Test
    fun `plain helper returns plain component`() {
        val component = DaisyText.plain("Daisy")

        assertEquals(Component.text("Daisy"), component)
    }

    @Test
    fun `string placeholders render deterministically`() {
        assertEquals("Hello Daisy", "Hello {name}".withPlaceholders("name" to "Daisy"))
    }

    @Test
    fun `component placeholders render deterministically`() {
        val component = Component.text("Hello {name}").withPlaceholders("name" to "Daisy")

        assertEquals(Component.text("Hello ").append(Component.text("Daisy")), component)
    }

    @Test
    fun `text source render helpers parse minimessage`() {
        val source =
            object : DaisyTextSource {
                override fun text(key: String): String? = if (key == "title") "<pink>{name}</pink>" else null

                override fun textList(key: String): List<String> =
                    if (key == "lines") {
                        listOf("<gray>{name}</gray>", "<white>done</white>")
                    } else {
                        emptyList()
                    }
            }

        assertEquals(DaisyText.parse("<pink>Daisy</pink>"), source.render("title", "name" to "Daisy"))
        assertEquals(
            listOf(DaisyText.parse("<gray>Daisy</gray>"), DaisyText.parse("<white>done</white>")),
            source.renderList("lines", "name" to "Daisy"),
        )
    }

    @Test
    fun `owned message bindings clear safely`() {
        val owner = Any()
        val source =
            object : DaisyTextSource {
                override fun text(key: String): String? = if (key == "hello") "hi" else null

                override fun textList(key: String): List<String> = emptyList()
            }

        DaisyMessages.install(owner, source)
        assertEquals("hi", DaisyMessages.resolve("hello"))

        DaisyMessages.clear(Any())
        assertEquals("hi", DaisyMessages.resolve("hello"))

        DaisyMessages.clear(owner)
        assertNull(DaisyMessages.resolve("hello"))
    }
}
