package cat.daisy.example

import cat.daisy.command.DaisyCommandGroup
import cat.daisy.command.DaisyCommandSet
import cat.daisy.command.replyResult
import cat.daisy.menu.openMenu
import org.bukkit.Material
import java.util.concurrent.CompletableFuture

@DaisyCommandSet
object ProfileCommands : DaisyCommandGroup({
    command("profile") {
        description("Open the DaisyCore profile example")
        aliases("me")

        player {
            replyLang("messages.profile.opening", "player" to player.name)
            player.openMenu(lang("menus.profile.title"), rows = 3) {
                background(Material.GRAY_STAINED_GLASS_PANE) {
                    name(" ")
                }

                slot(13) {
                    item(Material.PLAYER_HEAD) {
                        skullOwner(player)
                        nameLang("menus.profile.card.name", viewer = player, "player" to player.name)
                        loreLang("menus.profile.card.lore", viewer = player, "player" to player.name)
                    }
                    messageLang("messages.profile.clicked", "player" to player.name)
                    closeOnClick()
                }
            }
            DaisyCoreExamplePlugin.platform.scoreboards?.show(player, profileSidebar(player))
            DaisyCoreExamplePlugin.platform.tablists?.show(player, profileTablist(player))
        }

        subcommand("sync") {
            player {
                replyLang("messages.profile.loading")
                CompletableFuture
                    .completedFuture(Result.success(player.name))
                    .replyResult(
                        context = this,
                        success = { synced ->
                            replyLang("messages.profile.synced", "player" to synced)
                            DaisyCoreExamplePlugin.platform.scoreboards?.session(player)?.invalidate("coins")
                            DaisyCoreExamplePlugin.platform.tablists?.session(player)?.refreshNow()
                            replyLang("messages.profile.sidebar-refreshed", "player" to synced)
                        },
                        failure = { error ->
                            failLang("messages.profile.error", "error" to error.message.orEmpty())
                        },
                    )
            }
        }
    }
})
