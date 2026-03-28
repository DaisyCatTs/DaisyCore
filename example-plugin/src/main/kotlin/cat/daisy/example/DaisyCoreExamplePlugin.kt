package cat.daisy.example

import cat.daisy.core.platform.DaisyPlatform
import org.bukkit.plugin.java.JavaPlugin

class DaisyCoreExamplePlugin : JavaPlugin() {
    private lateinit var daisy: DaisyPlatform

    override fun onEnable() {
        daisy =
            DaisyPlatform.create(this) {
                text()
                placeholders()
                items()
                scoreboards()
                tablists()
                commands()
                menus()
            }
    }

    override fun onDisable() {
        daisy.close()
    }
}
