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
    private var binding: DaisyMessageBinding? = null

    public fun install(resolver: DaisyMessageResolver) {
        binding = DaisyMessageBinding(owner = null, resolver = resolver, textSource = null)
    }

    public fun install(textSource: DaisyTextSource) {
        binding = DaisyMessageBinding(owner = null, resolver = textSource, textSource = textSource)
    }

    public fun install(
        owner: Any,
        textSource: DaisyTextSource,
    ) {
        binding = DaisyMessageBinding(owner = owner, resolver = textSource, textSource = textSource)
    }

    public fun clear(owner: Any) {
        if (binding?.owner === owner) {
            clear()
        }
    }

    public fun clear() {
        binding = null
    }

    public fun resolve(key: String): String? = binding?.textSource?.text(key) ?: binding?.resolver?.resolve(key)

    public fun resolveList(key: String): List<String> = binding?.textSource?.textList(key) ?: emptyList()
}

private data class DaisyMessageBinding(
    val owner: Any?,
    val resolver: DaisyMessageResolver,
    val textSource: DaisyTextSource?,
)

public fun DaisyTextSource.render(
    key: String,
    vararg placeholders: Pair<String, Any?>,
): Component = DaisyText.parse(text(key)?.withPlaceholders(*placeholders) ?: key)

public fun DaisyTextSource.renderList(
    key: String,
    vararg placeholders: Pair<String, Any?>,
): List<Component> = textList(key).map { DaisyText.parse(it.withPlaceholders(*placeholders)) }
