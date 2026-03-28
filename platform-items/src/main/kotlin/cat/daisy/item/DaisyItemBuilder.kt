package cat.daisy.item

import cat.daisy.text.DaisyText
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.lang.reflect.Method

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
        name = DaisyText.parse(resolveViewerPlaceholders(text, viewer))
    }

    public fun lore(line: String) {
        lore += DaisyText.parse(line)
    }

    public fun lore(
        line: String,
        viewer: Player,
    ) {
        lore += DaisyText.parse(resolveViewerPlaceholders(line, viewer))
    }

    public fun loreMm(lines: List<String>) {
        lore += lines.map(DaisyText::parse)
    }

    public fun loreMm(
        lines: List<String>,
        viewer: Player,
    ) {
        lore += lines.map { DaisyText.parse(resolveViewerPlaceholders(it, viewer)) }
    }

    public fun build(): DaisyItemSpec =
        DaisyItemSpec(
            material = material,
            name = name,
            lore = lore.toList(),
        )
}

private var placeholderMethod: Method? = null
private var placeholderApiAvailable: Boolean? = null

private fun resolveViewerPlaceholders(
    input: String,
    player: Player?,
): String {
    if (player == null) {
        return input
    }
    val available =
        placeholderApiAvailable ?: run {
            val installed = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
            placeholderApiAvailable = installed
            installed
        }
    if (!available) {
        return input
    }

    val method =
        placeholderMethod ?: runCatching {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                .getMethod("setPlaceholders", OfflinePlayer::class.java, String::class.java)
        }.getOrNull().also { placeholderMethod = it }
    return runCatching {
        method?.invoke(null, player, input) as? String ?: input
    }.getOrDefault(input)
}

public fun item(
    material: Material,
    block: DaisyItemBuilder.() -> Unit = {},
): DaisyItemSpec = DaisyItemBuilder(material).apply(block).build()
