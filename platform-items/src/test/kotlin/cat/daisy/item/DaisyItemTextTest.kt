package cat.daisy.item

import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyTextSource
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import kotlin.test.Test
import kotlin.test.assertEquals

class DaisyItemTextTest {
    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `nameLang and loreLang render through installed text source`() {
        DaisyMessages.install(
            object : DaisyTextSource {
                override fun text(key: String): String? = if (key == "item.name") "<#f38ba8>{name}</#f38ba8>" else null

                override fun textList(key: String): List<String> =
                    if (key == "item.lore") {
                        listOf("<gray>{name}</gray>", "<#ffffff>ready</#ffffff>")
                    } else {
                        emptyList()
                    }
            },
        )

        val spec =
            item(Material.BOOK) {
                nameLang("item.name", placeholders = arrayOf("name" to "Profile"))
                loreLang("item.lore", placeholders = arrayOf("name" to "Profile"))
            }

        assertEquals("Profile", plain.serialize(spec.name!!))
        assertEquals(listOf("Profile", "ready"), spec.lore.map(plain::serialize))

        DaisyMessages.clear()
    }
}
