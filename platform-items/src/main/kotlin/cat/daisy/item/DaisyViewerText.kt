package cat.daisy.item

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.lang.reflect.Method

public object DaisyViewerText {
    private var placeholderMethod: Method? = null
    private var placeholderApiAvailable: Boolean? = null

    public fun render(
        input: String,
        viewer: Player?,
    ): String {
        if (viewer == null) {
            return input
        }
        if (!isPlaceholderApiAvailable()) {
            return input
        }

        val method =
            placeholderMethod ?: runCatching {
                Class.forName("me.clip.placeholderapi.PlaceholderAPI")
                    .getMethod("setPlaceholders", OfflinePlayer::class.java, String::class.java)
            }.getOrNull().also { placeholderMethod = it }

        return runCatching {
            method?.invoke(null, viewer, input) as? String ?: input
        }.getOrDefault(input)
    }

    public fun render(
        lines: List<String>,
        viewer: Player?,
    ): List<String> = lines.map { render(it, viewer) }

    private fun isPlaceholderApiAvailable(): Boolean =
        placeholderApiAvailable ?: run {
            val installed = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null
            placeholderApiAvailable = installed
            installed
        }
}
