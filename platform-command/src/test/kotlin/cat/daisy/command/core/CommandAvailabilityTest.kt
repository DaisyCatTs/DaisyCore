package cat.daisy.command.core

import cat.daisy.command.DaisyCommandAvailabilityContext
import cat.daisy.command.dsl.command
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.bukkit.plugin.java.JavaPlugin
import kotlin.test.assertEquals

class CommandAvailabilityTest {
    private val plugin = mock(JavaPlugin::class.java)
    private val context = DaisyCommandAvailabilityContext(plugin)

    @Test
    fun `disabled roots are removed before registration`() {
        val commands =
            listOf(
                command("alpha") {
                    ignore()
                    execute { }
                },
                command("beta") {
                    execute { }
                },
            )

        val filtered = CommandAvailabilityFilter.filter(commands, context)

        assertEquals(listOf("beta"), filtered.map(CommandSpec::name))
    }

    @Test
    fun `disabled subcommands are removed before validation`() {
        val commands =
            listOf(
                command("island") {
                    sub("create") {
                        ignore()
                        execute { }
                    }
                    sub("home") {
                        execute { }
                    }
                },
            )

        val filtered = CommandAvailabilityFilter.filter(commands, context)

        assertEquals(listOf("home"), filtered.single().children.map(CommandNodeSpec::name))
    }

    @Test
    fun `roots with no active handler and no active children are dropped`() {
        val commands =
            listOf(
                command("empty") {
                    sub("disabled") {
                        ignore()
                        execute { }
                    }
                },
            )

        val filtered = CommandAvailabilityFilter.filter(commands, context)

        assertEquals(emptyList(), filtered)
    }

    @Test
    fun `enabled predicate can keep one duplicate root active`() {
        val commands =
            listOf(
                command("shared") {
                    enabled { false }
                    execute { }
                },
                command("shared") {
                    execute { }
                },
            )

        val filtered = CommandAvailabilityFilter.filter(commands, context)

        assertEquals(1, filtered.size)
        assertEquals("shared", filtered.single().name)
    }
}
