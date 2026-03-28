# DaisyCore

DaisyCore is the one-library approach for Paper plugins. It brings commands, menus, scoreboards, tablists, text, placeholders, items, and runtime infrastructure into one Kotlin-first dependency while keeping the internal codebase modular.

## Why DaisyCore

- One main dependency for your server projects.
- One consistent Kotlin-first API style across commands, menus, scoreboards, and tablists.
- Internal module boundaries that keep the platform maintainable as it grows.
- Adventure-first text handling and modern Paper-first design.
- Shared runtime, placeholder, and item systems so features compose cleanly.
- Auto-loaded command providers instead of per-plugin registration boilerplate.

## Modules At A Glance

| Module | Purpose |
| --- | --- |
| `platform-base` | Shared contracts, context, cleanup primitives |
| `platform-runtime` | Lifecycle, scheduler, batching, shutdown coordination |
| `platform-text` | Adventure + MiniMessage text layer |
| `platform-placeholders` | Placeholder contracts and resolution pipeline |
| `platform-items` | Item builders and item text helpers |
| `platform-command` | Kotlin-first command system |
| `platform-menu` | Menu/view framework |
| `platform-scoreboard` | Sidebar, team, and objective runtime |
| `platform-tablist` | Header/footer and tab view runtime |
| `platform-packet` | Optional packet-backed transports |
| `platform-all` | The umbrella artifact published as `DaisyCore` |

## Installation

```kotlin
repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    implementation("cat.daisy:DaisyCore:1.0.0")
}
```

## Quick Start

```kotlin
class MyPlugin : JavaPlugin() {
    private lateinit var daisy: DaisyPlatform

    override fun onEnable() {
        daisy =
            DaisyPlatform.create(this) {
                text()
                placeholders()
                items()
                scoreboards()
                tablists()
                commands()
                menus()
            }
    }

    override fun onDisable() {
        daisy.close()
    }
}
```

## Commands

```kotlin
@DaisyCommandSet
object IslandCommands : DaisyCommandProvider {
    override fun commands(): List<DaisyCommand> =
        listOf(
            command("island") {
                description("Island management")

                sub("create") {
                    executePlayer {
                        reply("Island created.")
                    }
                }
            },
        )
}
```

## Menus

```kotlin
val menu =
    menu("Skyblock", rows = 3) {
        slot(13) {
            item(Material.GRASS_BLOCK) {
                name = "Your Island"
            }
        }
    }
```

## Scoreboards

```kotlin
val sidebar =
    sidebar {
        title(DaisyText.plain("Skyblock"))
        line("coins") { DaisyText.plain("Coins: 1,250") }
        blank()
        line("online") { DaisyText.plain("Online: 42") }
    }
```

## Tablists

```kotlin
val tab =
    tablist {
        header { DaisyText.plain("Welcome") }
        footer { DaisyText.plain("docs.daisy.cat") }
    }
```

## Runtime Principles

- Batch same-tick runtime work.
- Diff render state before sending updates.
- Prefer keyed invalidation over full redraws.
- Keep optional packet support behind dedicated transports.
- Guarantee deterministic shutdown and session cleanup.

## Status

DaisyCore is now structured as one umbrella dependency backed by internal modules. Commands and menus have been consolidated into the repo, and the platform bootstrap, scoreboard runtime, and tablist runtime are in place for continued production hardening.

## Documentation

Documentation lives at [docs.daisy.cat](https://docs.daisy.cat). The docs repo is kept separately in `DaisyCoreDocs`.

## Migration

If you already use `DaisyCommand` or `DaisyMenu`, DaisyCore is the new home. Migration notes are tracked in [MIGRATION.md](MIGRATION.md) and expanded in the docs site.

## Ecosystem Roadmap

The next Daisy-owned utility layers are tracked in [DAISY_SUITE_ROADMAP.md](DAISY_SUITE_ROADMAP.md). That roadmap covers the planned DaisySeries/XSuite-style libraries that should grow on top of DaisyCore rather than beside it.

## License

MIT. See [LICENSE](LICENSE).
