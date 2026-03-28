# DaisyCore

DaisyCore is an all-in-one Kotlin-first Paper platform for serious Minecraft server development. It brings commands, menus, scoreboards, tablists, text, placeholders, items, and runtime infrastructure into one coherent dependency while keeping the codebase internally modular and production-friendly.

## Why DaisyCore

- One main dependency for your server projects.
- One consistent Kotlin-first API style across commands, menus, scoreboards, and tablists.
- Internal module boundaries that keep the platform maintainable as it grows.
- Adventure-first text handling and modern Paper-first design.
- Shared runtime, placeholder, and item systems so features compose cleanly.

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
    implementation("cat.daisy:DaisyCore:0.1.0-SNAPSHOT")
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

## Performance Principles

- Batch same-tick runtime work.
- Diff render state before sending updates.
- Prefer keyed invalidation over full redraws.
- Keep optional packet support behind dedicated transports.
- Guarantee deterministic shutdown and session cleanup.

## Status

This repository currently provides the production scaffold and first public contracts for DaisyCore. The platform modules are wired and ready for iterative feature implementation.

## Documentation

The docs site lives separately in `DaisyCoreDocs` and is designed for Astro + Starlight deployment on Cloudflare Pages with Wrangler.

## Ecosystem Roadmap

The next Daisy-owned utility layers are tracked in [DAISY_SUITE_ROADMAP.md](DAISY_SUITE_ROADMAP.md). That roadmap covers the planned DaisySeries/XSuite-style libraries that should grow on top of DaisyCore rather than beside it.

## License

MIT. See [LICENSE](LICENSE).
