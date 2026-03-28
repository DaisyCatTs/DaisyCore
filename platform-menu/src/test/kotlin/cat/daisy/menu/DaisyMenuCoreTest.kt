package cat.daisy.menu

import net.kyori.adventure.text.Component
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
            assertTrue(DaisyMenu.getOpenMenuCount() >= 1)

            DaisyMenu.shutdown()
            assertEquals(0, DaisyMenu.getOpenMenuCount())
        }
    }

    private val createdTasks = mutableListOf<BukkitTask>()

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

            DaisyMenu.initialize(plugin)
            try {
                MenuTestContext(createdInventories).block(scheduler, plugin, pluginManager, taskCaptor, createInventory)
            } finally {
                if (DaisyMenu.isInitialized()) {
                    DaisyMenu.shutdown()
                }
            }
        }
    }

    private fun createPlayer(name: String): Player {
        val player = mock(Player::class.java)
        val openView = mock(InventoryView::class.java)
        val topInventory = mock(Inventory::class.java)
        `when`(player.name).thenReturn(name)
        `when`(player.uniqueId).thenReturn(UUID.nameUUIDFromBytes(name.toByteArray()))
        `when`(player.isOnline).thenReturn(true)
        `when`(openView.topInventory).thenReturn(topInventory)
        `when`(player.openInventory).thenReturn(openView)
        doReturn(openView).`when`(player).openInventory(org.mockito.ArgumentMatchers.any(Inventory::class.java))
        return player
    }

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
