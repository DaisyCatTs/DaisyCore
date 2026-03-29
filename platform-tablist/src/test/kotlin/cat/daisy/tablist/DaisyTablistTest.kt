package cat.daisy.tablist

import cat.daisy.core.DaisyHandle
import cat.daisy.core.runtime.DaisyBatcher
import cat.daisy.core.runtime.DaisyRuntime
import cat.daisy.core.runtime.DaisyScheduler
import cat.daisy.placeholder.DaisyPlaceholderRegistry
import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyText
import cat.daisy.text.DaisyTextSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.time.Duration
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DaisyTablistTest {
    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `header and footer are optional`() {
        val built = tablist {}

        assertNull(built.header)
        assertNull(built.footer)
    }

    @Test
    fun `options builder updates defaults and string overloads`() {
        val built =
            tablist {
                header("<#f38ba8>Welcome")
                footer(DaisyText.plain("Footer"))
                options {
                    updateInterval = Duration.ofSeconds(2)
                    autoRefresh = false
                }
            }

        assertNotNull(built.header)
        assertNotNull(built.footer)
        assertEquals(Duration.ofSeconds(2), built.options.updateInterval)
        assertEquals(false, built.options.autoRefresh)
    }

    @Test
    fun `headerLang and footerLang render through shared text source`() {
        DaisyMessages.install(
            object : DaisyTextSource {
                override fun text(key: String): String? =
                    when (key) {
                        "tab.header" -> "<#f38ba8>Hello {player}"
                        "tab.footer" -> "<white>Coins: {coins}"
                        else -> null
                    }

                override fun textList(key: String): List<String> = emptyList()
            },
        )

        try {
            val player = createPlayer("tablist-lang")
            val built =
                tablist {
                    headerLang("tab.header", viewer = player, "player" to player.name)
                    footerLang("tab.footer", viewer = player, "coins" to 1250)
                }

            val sent = mutableListOf<Pair<String, String>>()
            withTablistPlatform(sent, player) { platform ->
                val session = platform.show(player, built)

                assertTrue(sent.any { it.first == "Hello tablist-lang" && it.second == "Coins: 1250" })
                session.close()
                platform.close()
            }
        } finally {
            DaisyMessages.clear()
        }
    }

    @Test
    fun `tablist sessions refresh close and clean up on player close`() {
        val sent = mutableListOf<Pair<String, String>>()
        val player = createPlayer("tablist-runtime")
        var footerValue = "1,250"

        val built =
            tablist {
                header { DaisyText.plain("Profile Hub") }
                footer { DaisyText.plain("Coins: $footerValue") }
                options {
                    autoRefresh = false
                }
            }

        withTablistPlatform(sent, player) { platform ->
            val session = platform.show(player, built)

            assertNotNull(platform.session(player))
            assertTrue(sent.any { it.first == "Profile Hub" && it.second == "Coins: 1,250" })

            footerValue = "2,000"
            session.refreshNow()
            assertTrue(sent.any { it.second == "Coins: 2,000" })

            platform.close(player)
            assertNull(platform.session(player))
            assertEquals("" to "", sent.last())

            platform.close()
            verify(player, atLeastOnce()).sendPlayerListHeaderAndFooter(any(Component::class.java), any(Component::class.java))
        }
    }

    private fun withTablistPlatform(
        sent: MutableList<Pair<String, String>>,
        player: Player,
        block: (DaisyTablistPlatformImpl) -> Unit,
    ) {
        val plugin = mock(Plugin::class.java)
        val pluginManager = mock(PluginManager::class.java)
        val logger = Logger.getLogger("DaisyTablistTest")
        `when`(plugin.logger).thenReturn(logger)

        doAnswer {
            sent += plain.serialize(it.getArgument<Component>(0)) to plain.serialize(it.getArgument<Component>(1))
            null
        }.`when`(player).sendPlayerListHeaderAndFooter(any(Component::class.java), any(Component::class.java))

        val scheduler =
            object : DaisyScheduler {
                override fun run(task: () -> Unit): DaisyHandle {
                    task()
                    return handle()
                }

                override fun later(
                    delay: Duration,
                    task: () -> Unit,
                ): DaisyHandle {
                    task()
                    return handle()
                }

                override fun repeating(
                    period: Duration,
                    task: () -> Unit,
                ): DaisyHandle = handle()

                override fun main(task: () -> Unit): DaisyHandle {
                    task()
                    return handle()
                }
            }

        val runtime =
            object : DaisyRuntime {
                override val scheduler: DaisyScheduler = scheduler
                override val batcher: DaisyBatcher = mock(DaisyBatcher::class.java)
                override val logger: Logger = logger

                override fun register(handle: DaisyHandle): DaisyHandle = handle
            }

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(pluginManager)
            val platform = DaisyTablistPlatformImpl(plugin, runtime, DaisyPlaceholderRegistry())
            block(platform)
        }
    }

    private fun createPlayer(name: String): Player {
        val player = mock(Player::class.java)
        val world = mock(World::class.java)
        `when`(player.name).thenReturn(name)
        `when`(player.uniqueId).thenReturn(UUID.nameUUIDFromBytes(name.toByteArray()))
        `when`(player.world).thenReturn(world)
        `when`(world.name).thenReturn("world")
        `when`(player.isOnline).thenReturn(true)
        return player
    }

    private fun handle(): DaisyHandle = DaisyHandle {}
}
