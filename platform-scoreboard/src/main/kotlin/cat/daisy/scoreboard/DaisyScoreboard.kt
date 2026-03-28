package cat.daisy.scoreboard

import cat.daisy.text.DaisyTextRenderer
import net.kyori.adventure.text.Component

public data class DaisySidebarLine(
    val key: String,
    val renderer: DaisyTextRenderer<SidebarContext>,
)

public data class SidebarContext(
    val viewerName: String,
)

public data class DaisySidebarSpec(
    val title: Component,
    val lines: List<DaisySidebarLine>,
)

public class DaisySidebarBuilder(
    private val title: Component,
) {
    private val lines = mutableListOf<DaisySidebarLine>()

    public fun line(
        key: String,
        renderer: DaisyTextRenderer<SidebarContext>,
    ) {
        lines += DaisySidebarLine(key, renderer)
    }

    internal fun build(): DaisySidebarSpec = DaisySidebarSpec(title = title, lines = lines.toList())
}

public fun sidebar(
    title: Component,
    block: DaisySidebarBuilder.() -> Unit,
): DaisySidebarSpec = DaisySidebarBuilder(title).apply(block).build()
