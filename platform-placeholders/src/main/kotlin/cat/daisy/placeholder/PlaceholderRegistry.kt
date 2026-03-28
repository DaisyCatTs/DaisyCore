package cat.daisy.placeholder

import cat.daisy.core.DaisyAudienceContext

public class PlaceholderRegistry {
    private val resolvers = LinkedHashMap<String, PlaceholderResolver>()

    public fun register(
        key: String,
        resolver: PlaceholderResolver,
    ) {
        resolvers[key.lowercase()] = resolver
    }

    public fun resolve(
        key: String,
        context: DaisyAudienceContext,
    ): String? = resolvers[key.lowercase()]?.resolve(key, context)
}
