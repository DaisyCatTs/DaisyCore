package cat.daisy.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

public object DaisyText {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    public fun parse(input: String): Component = miniMessage.deserialize(input)

    public fun plain(input: String): Component = Component.text(input)
}
