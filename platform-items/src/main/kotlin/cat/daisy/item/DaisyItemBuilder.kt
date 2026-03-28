package cat.daisy.item

import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyText
import cat.daisy.text.withPlaceholders
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.entity.Player

public class DaisyItemBuilder(
    private val material: Material,
) {
    public var name: Component? = null
    private val lore: MutableList<Component> = mutableListOf()

    public fun lore(line: Component) {
        lore += line
    }

    public fun name(text: String) {
        name = DaisyText.parse(text)
    }

    public fun nameMm(text: String) {
        name = DaisyText.parse(text)
    }

    public fun name(
        text: String,
        viewer: Player,
    ) {
        name = DaisyText.parse(DaisyViewerText.render(text, viewer))
    }

    public fun nameLang(
        key: String,
        viewer: Player? = null,
        vararg placeholders: Pair<String, Any?>,
    ) {
        val text = DaisyMessages.resolve(key)?.withPlaceholders(*placeholders) ?: key
        if (viewer != null) {
            name(text, viewer)
        } else {
            name(text)
        }
    }

    public fun lore(line: String) {
        lore += DaisyText.parse(line)
    }

    public fun lore(
        line: String,
        viewer: Player,
    ) {
        lore += DaisyText.parse(DaisyViewerText.render(line, viewer))
    }

    public fun loreMm(lines: List<String>) {
        lore += lines.map(DaisyText::parse)
    }

    public fun loreMm(
        lines: List<String>,
        viewer: Player,
    ) {
        lore += DaisyViewerText.render(lines, viewer).map(DaisyText::parse)
    }

    public fun loreLang(
        key: String,
        viewer: Player? = null,
        vararg placeholders: Pair<String, Any?>,
    ) {
        val lines = DaisyMessages.resolveList(key).map { it.withPlaceholders(*placeholders) }
        if (viewer != null) {
            lore += DaisyViewerText.render(lines, viewer).map(DaisyText::parse)
        } else {
            lore += lines.map(DaisyText::parse)
        }
    }

    public fun build(): DaisyItemSpec =
        DaisyItemSpec(
            material = material,
            name = name,
            lore = lore.toList(),
        )
}

public fun item(
    material: Material,
    block: DaisyItemBuilder.() -> Unit = {},
): DaisyItemSpec = DaisyItemBuilder(material).apply(block).build()
