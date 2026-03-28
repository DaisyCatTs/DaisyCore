package cat.daisy.core.platform

import cat.daisy.core.DaisyFeature
import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyTextSource
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DaisyPlatformBuilderTest {
    @Test
    fun `builder captures selected features`() {
        val plugin = mock<Plugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("test"))
        val platform =
            DaisyPlatform.create(plugin) {
                text()
                placeholders()
            }

        assertEquals(
            setOf(
                DaisyFeature.TEXT,
                DaisyFeature.PLACEHOLDERS,
            ),
            platform.features,
        )
    }

    @Test
    fun `placeholders are created when requested`() {
        val plugin = mock<Plugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("test"))

        val platform =
            DaisyPlatform.create(plugin) {
                placeholders()
            }

        assertNotNull(platform.placeholders)
        assertNull(platform.commands)
        platform.close()
    }

    @Test
    fun `commands create a registry for java plugins`() {
        val plugin = mock<JavaPlugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("test"))

        val platform =
            DaisyPlatform.create(plugin) {
                commands()
            }

        assertNotNull(platform.commands)
        assertNull(platform.menus)
        platform.close()
    }

    @Test
    fun `menus scoreboards and tablists initialize when requested`() {
        val plugin = mock<Plugin>()
        val pluginManager = mock(PluginManager::class.java)
        whenever(plugin.logger).thenReturn(Logger.getLogger("test"))

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(pluginManager)

            val platform =
                DaisyPlatform.create(plugin) {
                    menus()
                    scoreboards()
                    tablists()
                }

            assertNotNull(platform.menus)
            assertNotNull(platform.scoreboards)
            assertNotNull(platform.tablists)
            platform.close()
        }
    }

    @Test
    fun `messages source installs and clears with platform lifecycle`() {
        val plugin = mock<Plugin>()
        whenever(plugin.logger).thenReturn(Logger.getLogger("test"))
        val source =
            object : DaisyTextSource {
                override fun text(key: String): String? = if (key == "hello") "<pink>Hello" else null

                override fun textList(key: String): List<String> = if (key == "list") listOf("a", "b") else emptyList()
            }

        val platform =
            DaisyPlatform.create(plugin) {
                messages(source)
            }

        assertEquals(source, platform.messageSource)
        assertEquals("<pink>Hello", DaisyMessages.resolve("hello"))

        platform.close()

        assertNull(DaisyMessages.resolve("hello"))
    }
}
