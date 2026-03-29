package cat.daisy.scoreboard

import cat.daisy.core.DaisyAudienceContext
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
import org.bukkit.Server
import org.bukkit.World
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Score
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.ScoreboardManager
import org.bukkit.scoreboard.Team
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any as anyArg
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import java.time.Duration
import java.util.Locale
import java.util.UUID
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DaisySidebarTest {
    private val plain = PlainTextComponentSerializer.plainText()

    @Test
    fun `duplicate keys are rejected`() {
        assertFailsWith<IllegalArgumentException> {
            sidebar {
                line("coins", renderer = cat.daisy.text.DaisyTextRenderer { DaisyText.plain("1") })
                line("coins", renderer = cat.daisy.text.DaisyTextRenderer { DaisyText.plain("2") })
            }
        }
    }

    @Test
    fun `blank lines receive unique keys`() {
        val built =
            sidebar {
                blank()
                blank()
            }

        assertEquals(2, built.lines.size)
        assertEquals(2, built.lines.map(DaisySidebarLine::key).distinct().size)
    }

    @Test
    fun `sidebar line count is capped to transport limit`() {
        assertFailsWith<IllegalArgumentException> {
            sidebar {
                repeat(16) { index ->
                    line("line$index", renderer = cat.daisy.text.DaisyTextRenderer { DaisyText.plain(index.toString()) })
                }
            }
        }
    }

    @Test
    fun `titleLang and line builder render through shared text source`() {
        DaisyMessages.install(
            object : DaisyTextSource {
                override fun text(key: String): String? =
                    when (key) {
                        "sidebar.title" -> "<#f38ba8>Profile"
                        "sidebar.coins" -> "<white>Coins: <#f38ba8>{coins}</#f38ba8>"
                        "sidebar.online" -> "<white>Online"
                        else -> null
                    }

                override fun textList(key: String): List<String> = emptyList()
            },
        )

        try {
            val player = createPlayer("sidebar-lang")
            val pluginManager = mock(PluginManager::class.java)
            val built =
                sidebar {
                    titleLang("sidebar.title", viewer = player)
                    line("coins") {
                        textLang("sidebar.coins", viewer = player, "coins" to 1250)
                    }
                    line("online") {
                        visible { it.player.isOnline }
                        lang("sidebar.online", viewer = player)
                    }
                }

            val context = renderContext(player)
            mockStatic(Bukkit::class.java).use { bukkit ->
                bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(pluginManager)
                assertEquals("Profile", plain.serialize(built.title.render(context)))
                assertEquals("Coins: 1250", plain.serialize(built.lines.first { it.key == "coins" }.renderer.render(context)))
                assertTrue(built.lines.first { it.key == "online" }.visible(context))
            }
        } finally {
            DaisyMessages.clear()
        }
    }

    @Test
    fun `sidebar sessions refresh targeted keys and restore state on close`() {
        withSidebarEnvironment { plugin, runtime, player, previousScoreboard, scoreboard, objective, teamPrefixes, teamUnregisters ->
            var coins = "1,250"
            val built =
                sidebar {
                    title("Profile")
                    line("coins", renderer = cat.daisy.text.DaisyTextRenderer { DaisyText.plain("Coins: $coins") })
                    line("online") {
                        text(DaisyText.plain("Online"))
                    }
                    options {
                        autoRefresh = false
                        updateInterval = Duration.ofSeconds(2)
                    }
                }

            val platform = DaisyScoreboardPlatformImpl(plugin, runtime, DaisyPlaceholderRegistry())
            val session = platform.show(player, built)

            assertNotNull(platform.session(player))
            verify(objective, atLeastOnce()).displayName(anyArg(Component::class.java))
            verify(player, atLeastOnce()).setScoreboard(scoreboard)
            assertEquals("Coins: 1,250", teamPrefixes["coins"]?.last())

            coins = "2,000"
            session.invalidate("coins")
            assertEquals("Coins: 2,000", teamPrefixes["coins"]?.last())

            session.refreshNow()
            verify(objective, atLeastOnce()).displayName(anyArg(Component::class.java))

            session.close()
            verify(player, atLeastOnce()).setScoreboard(previousScoreboard)
            assertTrue(teamUnregisters["coins"] ?: 0 >= 1)

            platform.close()
        }
    }

    @Test
    fun `refreshNow clears lines that become invisible`() {
        withSidebarEnvironment { plugin, runtime, player, _, _, _, _, teamUnregisters ->
            var showOnline = true
            val built =
                sidebar {
                    title("Profile")
                    line("online") {
                        visible { showOnline }
                        text("Online")
                    }
                    options {
                        autoRefresh = false
                    }
                }

            val platform = DaisyScoreboardPlatformImpl(plugin, runtime, DaisyPlaceholderRegistry())
            val session = platform.show(player, built)
            showOnline = false
            session.refreshNow()

            assertTrue(teamUnregisters["online"] ?: 0 >= 1)
            platform.close()
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

    private fun renderContext(player: Player): DaisySidebarRenderContext =
        DaisySidebarRenderContext(
            player = player,
            audience = DaisyAudienceContext(player.uniqueId, Locale.ENGLISH, player.world.name),
            placeholders = DaisyPlaceholderRegistry(),
        )

    private fun withSidebarEnvironment(
        block: (
            plugin: Plugin,
            runtime: DaisyRuntime,
            player: Player,
            previousScoreboard: Scoreboard,
            scoreboard: Scoreboard,
            objective: Objective,
            teamPrefixes: MutableMap<String, MutableList<String>>,
            teamUnregisters: MutableMap<String, Int>,
        ) -> Unit,
    ) {
        val plugin = mock(Plugin::class.java)
        val pluginManager = mock(PluginManager::class.java)
        val scoreboardManager = mock(ScoreboardManager::class.java)
        val previousScoreboard = mock(Scoreboard::class.java)
        val objective = mock(Objective::class.java)
        val scoreboard =
            mock(Scoreboard::class.java) { invocation ->
                if (invocation.method.name == "registerNewObjective") {
                    objective
                } else {
                    org.mockito.Answers.RETURNS_DEFAULTS.answer(invocation)
                }
            }
        val player = createPlayer("sidebar-runtime")
        val teamPrefixes = mutableMapOf<String, MutableList<String>>()
        val teamUnregisters = mutableMapOf<String, Int>()
        val teams = linkedMapOf<String, Team>()
        val entries = linkedSetOf<String>()
        val logger = Logger.getLogger("DaisySidebarTest")
        `when`(plugin.logger).thenReturn(logger)
        `when`(player.scoreboard).thenReturn(previousScoreboard, scoreboard)

        `when`(scoreboardManager.newScoreboard).thenReturn(scoreboard)
        `when`(scoreboard.getTeam(anyString())).thenAnswer { teams[it.getArgument(0)] }
        `when`(scoreboard.registerNewTeam(anyString())).thenAnswer {
            val name = it.getArgument<String>(0)
            val team = mock(Team::class.java)
            val teamEntries = linkedSetOf<String>()
            `when`(team.name).thenReturn(name)
            `when`(team.entries).thenAnswer { teamEntries.toSet() }
            doAnswer {
                val entry = it.getArgument<String>(0)
                teamEntries += entry
                entries += entry
                null
            }.`when`(team).addEntry(anyString())
            doAnswer { teamEntries.remove(it.getArgument<String>(0)); null }.`when`(team).removeEntry(anyString())
            doAnswer {
                val component = it.getArgument<Component>(0)
                teamPrefixes.getOrPut(name) { mutableListOf() } += plain.serialize(component)
                null
            }.`when`(team).prefix(anyArg(Component::class.java))
            doAnswer {
                teamUnregisters[name] = (teamUnregisters[name] ?: 0) + 1
                teams.remove(name)
                null
            }.`when`(team).unregister()
            teams[name] = team
            team
        }
        `when`(scoreboard.teams).thenAnswer { teams.values.toSet() }
        `when`(scoreboard.entries).thenAnswer { entries.toSet() }
        doAnswer { entries.remove(it.getArgument<String>(0)); null }.`when`(scoreboard).resetScores(anyString())
        `when`(scoreboard.objectives).thenReturn(setOf(objective))
        doAnswer { null }.`when`(objective).unregister()
        `when`(objective.getScore(anyString())).thenAnswer { mock(Score::class.java) }

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
            val server = mock(Server::class.java)
            bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(pluginManager)
            bukkit.`when`<Server> { Bukkit.getServer() }.thenReturn(server)
            bukkit.`when`<ScoreboardManager> { Bukkit.getScoreboardManager() }.thenReturn(scoreboardManager)
            block(plugin, runtime, player, previousScoreboard, scoreboard, objective, teamPrefixes, teamUnregisters)
        }
    }

    private fun handle(): DaisyHandle = DaisyHandle {}
}
