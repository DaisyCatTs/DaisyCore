package cat.daisy.text

import net.kyori.adventure.text.Component

public fun interface DaisyMessageResolver {
    public fun resolve(key: String): String?
}

public interface DaisyTextSource : DaisyMessageResolver {
    public fun text(key: String): String?

    public fun textList(key: String): List<String>

    override fun resolve(key: String): String? = text(key)
}

public object DaisyMessages {
    private var resolver: DaisyMessageResolver? = null
    private var textSource: DaisyTextSource? = null

    public fun install(resolver: DaisyMessageResolver) {
        this.resolver = resolver
    }

    public fun install(textSource: DaisyTextSource) {
        this.textSource = textSource
        this.resolver = textSource
    }

    public fun clear() {
        resolver = null
        textSource = null
    }

    public fun resolve(key: String): String? = textSource?.text(key) ?: resolver?.resolve(key)

    public fun resolveList(key: String): List<String> = textSource?.textList(key) ?: emptyList()
}

public fun DaisyTextSource.render(
    key: String,
    vararg placeholders: Pair<String, Any?>,
): Component = DaisyText.parse(text(key)?.withPlaceholders(*placeholders) ?: key)

public fun DaisyTextSource.renderList(
    key: String,
    vararg placeholders: Pair<String, Any?>,
): List<Component> = textList(key).map { DaisyText.parse(it.withPlaceholders(*placeholders)) }
