package cat.daisy.core.platform

import cat.daisy.core.DaisyFeature
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.bukkit.plugin.Plugin
import kotlin.test.assertEquals

class DaisyPlatformBuilderTest {
    @Test
    fun `builder captures selected features`() {
        val plugin = mock<Plugin>()
        val platform =
            DaisyPlatform.create(plugin) {
                text()
                scoreboards()
                tablists()
            }

        assertEquals(
            setOf(
                DaisyFeature.TEXT,
                DaisyFeature.SCOREBOARDS,
                DaisyFeature.TABLISTS,
            ),
            platform.features,
        )
    }
}
