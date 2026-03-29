package cat.daisy.example

import cat.daisy.core.platform.DaisyPlatform
import org.bukkit.plugin.java.JavaPlugin

class DaisyCoreExamplePlugin : JavaPlugin() {
    public companion object {
        public lateinit var platform: DaisyPlatform
            private set
    }

    private lateinit var daisy: DaisyPlatform

    override fun onEnable() {
        daisy =
            DaisyPlatform.create(this) {
                messages(ExampleTextSource)
                commands()
                menus()
                scoreboards()
                tablists()
            }
        platform = daisy
    }

    override fun onDisable() {
        daisy.close()
    }
}
