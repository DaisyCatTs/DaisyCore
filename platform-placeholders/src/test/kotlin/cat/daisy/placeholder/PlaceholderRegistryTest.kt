package cat.daisy.placeholder

import cat.daisy.core.DaisyAudienceContext
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class PlaceholderRegistryTest {
    @Test
    fun `registered resolvers are case insensitive`() {
        val registry = PlaceholderRegistry()
        registry.register("coins") { _, _ -> "150" }

        assertEquals("150", registry.resolve("COINS", DaisyAudienceContext(UUID.randomUUID())))
    }
}
