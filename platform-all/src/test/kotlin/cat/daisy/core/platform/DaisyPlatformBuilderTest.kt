package cat.daisy.core.platform

import cat.daisy.core.DaisyFeature
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.bukkit.plugin.Plugin
import java.util.logging.Logger
import kotlin.test.assertEquals

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
}
