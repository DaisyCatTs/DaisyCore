package cat.daisy.example

import cat.daisy.scoreboard.DaisySidebar
import cat.daisy.scoreboard.sidebar
import cat.daisy.tablist.DaisyTablist
import cat.daisy.tablist.tablist
import org.bukkit.entity.Player

internal fun profileSidebar(player: Player): DaisySidebar =
    sidebar {
        titleLang("sidebar.profile.title", viewer = player)
        line("name") { textLang("sidebar.profile.name", viewer = player, "player" to player.name) }
        line("rank") { textLang("sidebar.profile.rank", viewer = player) }
        line("coins") { textLang("sidebar.profile.coins", viewer = player, "coins" to "1,250") }
    }

internal fun profileTablist(player: Player): DaisyTablist =
    tablist {
        headerLang("tablist.profile.header", viewer = player, "player" to player.name)
        footerLang("tablist.profile.footer", viewer = player, "coins" to "1,250")
    }
