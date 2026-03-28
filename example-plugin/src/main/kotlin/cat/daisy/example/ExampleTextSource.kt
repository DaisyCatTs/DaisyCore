package cat.daisy.example

import cat.daisy.text.DaisyTextSource

object ExampleTextSource : DaisyTextSource {
    private val values =
        mapOf(
            "messages.profile.opening" to "<#f38ba8>Opening <white>{player}</white>'s profile.",
            "messages.profile.loading" to "<gray>Loading profile data...",
            "messages.profile.synced" to "<#f38ba8>Synced profile for <white>{player}</white>.",
            "messages.profile.error" to "<#ff6b6b>Error: <white>{error}</white>",
            "messages.profile.clicked" to "<#f38ba8>Clicked the profile card for <white>{player}</white>.",
            "menus.profile.title" to "<#f38ba8><bold>Profile</bold></#f38ba8>",
            "menus.profile.card.name" to "<#f38ba8><bold>{player}</bold></#f38ba8>",
        )

    private val lists =
        mapOf(
            "menus.profile.card.lore" to
                listOf(
                    "<gray>Inline Daisy menu flow",
                    "<white>Built for {player}",
                ),
        )

    override fun text(key: String): String? = values[key]

    override fun textList(key: String): List<String> = lists[key] ?: emptyList()
}
