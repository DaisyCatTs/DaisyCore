package cat.daisy.item

import net.kyori.adventure.text.Component
import org.bukkit.Material

public data class DaisyItemSpec(
    val material: Material,
    val name: Component? = null,
    val lore: List<Component> = emptyList(),
)
