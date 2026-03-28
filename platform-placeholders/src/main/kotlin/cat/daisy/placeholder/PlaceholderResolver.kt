package cat.daisy.placeholder

import cat.daisy.core.DaisyAudienceContext

public fun interface PlaceholderResolver {
    public fun resolve(
        key: String,
        context: DaisyAudienceContext,
    ): String?
}
