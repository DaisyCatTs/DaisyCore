package cat.daisy.core

import java.util.Locale
import java.util.UUID

/**
 * Shared per-viewer context for rendering and placeholder resolution.
 */
public data class DaisyAudienceContext(
    val audienceId: UUID,
    val locale: Locale = Locale.ENGLISH,
    val worldName: String? = null,
)
