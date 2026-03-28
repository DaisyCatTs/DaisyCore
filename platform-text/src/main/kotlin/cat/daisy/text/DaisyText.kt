package cat.daisy.text

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextReplacementConfig
import net.kyori.adventure.text.minimessage.MiniMessage

public object DaisyText {
    private val miniMessage: MiniMessage = MiniMessage.miniMessage()

    public fun parse(input: String): Component = miniMessage.deserialize(input)

    public fun plain(input: String): Component = Component.text(input)
}

public typealias DaisyPlaceholders = Array<out Pair<String, Any?>>

public fun String.withPlaceholders(vararg placeholders: Pair<String, Any?>): String =
    placeholders.fold(this) { acc, (key, value) ->
        acc.replace("{$key}", value?.toString() ?: "")
    }

public fun Component.withPlaceholders(vararg placeholders: Pair<String, Any?>): Component {
    var current = this
    placeholders.forEach { (key, value) ->
        current =
            current.replaceText(
                TextReplacementConfig.builder()
                    .matchLiteral("{$key}")
                    .replacement(value?.toString() ?: "")
                    .build(),
            )
    }
    return current
}
