package cat.daisy.item

import net.kyori.adventure.text.Component
import org.bukkit.Material

public class DaisyItemBuilder(
    private val material: Material,
) {
    public var name: Component? = null
    private val lore: MutableList<Component> = mutableListOf()

    public fun lore(line: Component) {
        lore += line
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
