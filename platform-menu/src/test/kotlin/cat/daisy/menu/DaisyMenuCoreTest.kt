package cat.daisy.menu

import cat.daisy.text.DaisyMessages
import cat.daisy.text.DaisyTextSource
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.InventoryView
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.bukkit.scheduler.BukkitScheduler
import org.bukkit.scheduler.BukkitTask
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.Mockito.doAnswer
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import java.util.UUID
import java.util.concurrent.atomic.AtomicInteger
import java.util.logging.Logger
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotSame
import kotlin.test.assertTrue

class DaisyMenuCoreTest {
    @Test
    fun `builder validation still rejects invalid definitions`() {
        assertFailsWith<IllegalArgumentException> {
            menu("   ") {}
        }
        assertFailsWith<IllegalArgumentException> {
            menu("Broken", rows = 0) {}
        }
        assertFailsWith<IllegalArgumentException> {
            menu("Pattern", rows = 1) {
                pattern("1234567890") {}
            }
        }
        assertFailsWith<IllegalArgumentException> {
            menu("Refresh", rows = 1) {
                slot(0) {
                    item = mock(ItemStack::class.java)
                    refreshEvery(20)
                }
            }
        }
    }

    @Test
    fun `menu sessions are isolated and static items are cloned`() {
        withMenuEnvironment { _, _, _, _, createInventory ->
            val original = mock(ItemStack::class.java)
            val clonedAtBuild = mock(ItemStack::class.java)
            val renderedForFirst = mock(ItemStack::class.java)
            val renderedForSecond = mock(ItemStack::class.java)
            val storedInFirst = mock(ItemStack::class.java)
            val storedInSecond = mock(ItemStack::class.java)
            `when`(original.clone()).thenReturn(clonedAtBuild)
            `when`(clonedAtBuild.clone()).thenReturn(renderedForFirst, renderedForSecond)
            `when`(renderedForFirst.clone()).thenReturn(storedInFirst, storedInFirst)
            `when`(renderedForSecond.clone()).thenReturn(storedInSecond, storedInSecond)

            val menu =
                menu("Shared", rows = 1) {
                    slot(0) {
                        item = original
                    }
                }

            val first = createPlayer("first")
            val second = createPlayer("second")

            val firstSession = first.openMenu(menu)
            val secondSession = second.openMenu(menu)

            assertNotSame(firstSession, secondSession)
            assertNotSame(firstSession.inventory, secondSession.inventory)
            verify(original, atLeastOnce()).clone()
            verify(clonedAtBuild, atLeastOnce()).clone()
        }
    }

    @Test
    fun `click handlers execute in declaration order through session runtime`() {
        withMenuEnvironment { _, _, _, _, _ ->
            val events = mutableListOf<String>()
            val player = createPlayer("clicker")
            val item = mock(ItemStack::class.java)
            `when`(item.clone()).thenReturn(item)

            val session =
                player.openMenu(
                    menu("Clicks", rows = 1) {
                        slot(0) {
                            this.item = item
                            onShiftClick { events += "shift" }
                            onLeftClick { events += "left" }
                            onClick(onMenuClick { events += "fallback" })
                        }
                    },
                )

            session.handleTopClick(0, ClickType.SHIFT_LEFT)
            session.handleTopClick(0, ClickType.LEFT)
            session.handleTopClick(0, ClickType.RIGHT)

            assertEquals(listOf("shift", "left", "fallback"), events)
        }
    }

    @Test
    fun `refresh tasks stop when a session closes and shutdown clears open sessions`() {
        withMenuEnvironment { scheduler, _, _, taskCaptor, _ ->
            val executions = AtomicInteger(0)
            val player = createPlayer("refresh")
            val session = player.openMenu(menu("Refresh", rows = 1) {})
            bindOpenInventory(player, session.inventory)

            session.refreshEvery(1) {
                executions.incrementAndGet()
            }

            val runnable = taskCaptor.allValues.last()
            runnable.run()
            runnable.run()
            assertEquals(2, executions.get())

            session.handleInventoryClose()
            verify(createdTasks.last(), times(1)).cancel()

            val otherPlayer = createPlayer("shutdown")
            val otherSession = otherPlayer.openMenu(menu("Shutdown", rows = 1) {})
            bindOpenInventory(otherPlayer, otherSession.inventory)
            assertTrue(DaisyMenuRuntime.getOpenMenuCount() >= 1)

            DaisyMenuRuntime.shutdown()
            assertEquals(0, DaisyMenuRuntime.getOpenMenuCount())
        }
    }

    @Test
    fun `inline open menu and click sugar stay concise`() {
        withMenuEnvironment { _, _, _, _, _ ->
            val player = createPlayer("inline")
            val sent = sentMessages(player)
            val display = mock(ItemStack::class.java)
            `when`(display.clone()).thenReturn(display)
            val closer = mock(ItemStack::class.java)
            `when`(closer.clone()).thenReturn(closer)

            val session =
                player.openMenu(title = "Inline", rows = 1) {
                    slot(0) {
                        item = display
                    }

                    slot(8) {
                        item = closer
                        messageMm("<#f38ba8>Closed")
                        closeOnClick()
                    }
                }

            bindOpenInventory(player, session.inventory)
            assertTrue(session.inventory.getItem(0) === display)
            assertTrue(session.inventory.getItem(8) === closer)

            session.handleTopClick(8, ClickType.LEFT)

            assertTrue(sent.any { it.contains("Closed") })
        }
    }

    @Test
    fun `messageLang uses the shared text source on click`() {
        DaisyMessages.install(
            object : DaisyTextSource {
                override fun text(key: String): String? = if (key == "clicked") "<pink>Hello {player}" else null

                override fun textList(key: String): List<String> = emptyList()
            },
        )

        try {
            withMenuEnvironment { _, _, _, _, _ ->
                val player = createPlayer("lang-click")
                val sent = sentMessages(player)
                val trigger = mock(ItemStack::class.java)
                `when`(trigger.clone()).thenReturn(trigger)

                val session =
                    player.openMenu(title = "Lang", rows = 1) {
                        slot(0) {
                            item = trigger
                            messageLang("clicked", "player" to player.name)
                        }
                    }

                session.handleTopClick(0, ClickType.LEFT)

                assertTrue(sent.any { it.contains("Hello lang-click") })
            }
        } finally {
            DaisyMessages.clear()
        }
    }

    @Test
    fun `refreshOnClick invalidates like the shorter alias promises`() {
        withMenuEnvironment { _, _, _, _, _ ->
            var count = 0
            val player = createPlayer("refresh-short")
            val trigger = mock(ItemStack::class.java)
            `when`(trigger.clone()).thenReturn(trigger)
            val session =
                player.openMenu(title = "Refresh Alias", rows = 1) {
                    slot(0) {
                        render {
                            val paper = mock(ItemStack::class.java)
                            `when`(paper.clone()).thenReturn(paper)
                            paper
                        }
                    }
                    slot(1) {
                        item = trigger
                        onClick {
                            count++
                        }
                        refreshOnClick(0)
                    }
                }

            assertTrue(session.inventory.getItem(0) != null)
            session.handleTopClick(1, ClickType.LEFT)
            verify(session.inventory, times(2)).setItem(org.mockito.ArgumentMatchers.eq(0), org.mockito.ArgumentMatchers.any())
        }
    }

    private val createdTasks = mutableListOf<BukkitTask>()
    private val messageStore = mutableMapOf<Player, MutableList<String>>()

    private fun withMenuEnvironment(
        block: MenuTestContext.(
            scheduler: BukkitScheduler,
            plugin: Plugin,
            pluginManager: PluginManager,
            taskCaptor: ArgumentCaptor<Runnable>,
            createInventory: () -> Inventory,
        ) -> Unit,
    ) {
        val plugin = mock(Plugin::class.java)
        val scheduler = mock(BukkitScheduler::class.java)
        val pluginManager = mock(PluginManager::class.java)
        val logger = Logger.getLogger("DaisyMenuCoreTest")
        `when`(plugin.logger).thenReturn(logger)
        createdTasks.clear()

        val taskCaptor = ArgumentCaptor.forClass(Runnable::class.java)
        doAnswer {
            val task = mock(BukkitTask::class.java)
            createdTasks += task
            task
        }.`when`(scheduler).runTaskTimer(
            org.mockito.ArgumentMatchers.eq(plugin),
            taskCaptor.capture(),
            org.mockito.ArgumentMatchers.anyLong(),
            org.mockito.ArgumentMatchers.anyLong(),
        )

        val createdInventories = mutableListOf<Inventory>()
        val createInventory = {
            val items = mutableMapOf<Int, org.bukkit.inventory.ItemStack?>()
            val inventory = mock(Inventory::class.java)
            doAnswer {
                items[it.getArgument<Int>(0)] = it.getArgument(1)
                null
            }.`when`(inventory).setItem(org.mockito.ArgumentMatchers.anyInt(), org.mockito.ArgumentMatchers.any())
            doAnswer { items[it.getArgument<Int>(0)] }.`when`(inventory).getItem(org.mockito.ArgumentMatchers.anyInt())
            `when`(inventory.size).thenReturn(9)
            createdInventories += inventory
            inventory
        }

        mockStatic(Bukkit::class.java).use { bukkit ->
            bukkit.`when`<Boolean> { Bukkit.isPrimaryThread() }.thenReturn(true)
            bukkit.`when`<BukkitScheduler> { Bukkit.getScheduler() }.thenReturn(scheduler)
            bukkit.`when`<PluginManager> { Bukkit.getPluginManager() }.thenReturn(pluginManager)
            bukkit.`when`<Inventory> {
                Bukkit.createInventory(
                    org.mockito.ArgumentMatchers.any(),
                    org.mockito.ArgumentMatchers.anyInt(),
                    org.mockito.ArgumentMatchers.any(Component::class.java),
                )
            }.thenAnswer { createInventory() }

            DaisyMenuRuntime.initialize(plugin)
            try {
                MenuTestContext(createdInventories).block(scheduler, plugin, pluginManager, taskCaptor, createInventory)
            } finally {
                messageStore.clear()
                if (DaisyMenuRuntime.isInitialized()) {
                    DaisyMenuRuntime.shutdown()
                }
            }
        }
    }

    private fun createPlayer(name: String): Player {
        val player = mock(Player::class.java)
        val openView = mock(InventoryView::class.java)
        val topInventory = mock(Inventory::class.java)
        val messages = mutableListOf<String>()
        `when`(player.name).thenReturn(name)
        `when`(player.uniqueId).thenReturn(UUID.nameUUIDFromBytes(name.toByteArray()))
        `when`(player.isOnline).thenReturn(true)
        `when`(openView.topInventory).thenReturn(topInventory)
        `when`(player.openInventory).thenReturn(openView)
        doReturn(openView).`when`(player).openInventory(org.mockito.ArgumentMatchers.any(Inventory::class.java))
        doAnswer {
            messages += PlainTextComponentSerializer.plainText().serialize(it.getArgument<Component>(0))
            null
        }.`when`(player).sendMessage(org.mockito.ArgumentMatchers.any(Component::class.java))
        messageStore[player] = messages
        return player
    }

    private fun sentMessages(player: Player): List<String> = messageStore[player] ?: emptyList()

    private fun bindOpenInventory(
        player: Player,
        inventory: Inventory,
    ) {
        val openView = mock(InventoryView::class.java)
        `when`(openView.topInventory).thenReturn(inventory)
        `when`(player.openInventory).thenReturn(openView)
    }

    private class MenuTestContext(
        private val inventories: List<Inventory>,
    )
}
