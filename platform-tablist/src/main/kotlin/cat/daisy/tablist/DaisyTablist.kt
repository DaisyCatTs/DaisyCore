package cat.daisy.tablist

import cat.daisy.text.DaisyTextRenderer
import net.kyori.adventure.text.Component

public data class TablistContext(
    val viewerName: String,
)

public data class DaisyTablistSpec(
    val header: DaisyTextRenderer<TablistContext>?,
    val footer: DaisyTextRenderer<TablistContext>?,
)

public class DaisyTablistBuilder {
    private var header: DaisyTextRenderer<TablistContext>? = null
    private var footer: DaisyTextRenderer<TablistContext>? = null

    public fun header(renderer: DaisyTextRenderer<TablistContext>) {
        header = renderer
    }

    public fun footer(renderer: DaisyTextRenderer<TablistContext>) {
        footer = renderer
    }

    internal fun build(): DaisyTablistSpec = DaisyTablistSpec(header = header, footer = footer)
}

public fun tablist(
    block: DaisyTablistBuilder.() -> Unit,
): DaisyTablistSpec = DaisyTablistBuilder().apply(block).build()
