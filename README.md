# DaisyCore

DaisyCore is the one-library approach for modern Paper plugins. It gives you commands, menus, scoreboards, tablists, text, placeholders, items, and runtime ownership behind one Kotlin-first dependency.

## Why DaisyCore

- One main dependency instead of stitching together separate command, menu, and UI libraries.
- One consistent API style across commands, menus, sidebars, and tablists.
- One platform owner through `DaisyPlatform`.
- Shared text and message rendering through `messages(...)`.
- Internal modules for maintainability without exposing users to fragmented setup.

## Install

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
                messages(MyTextSource)
                commands()
                menus()
                scoreboards()
                tablists()
            }
    }

    override fun onDisable() {
        daisy.close()
    }
}
```

`messages(...)` is the current default path because it lets the same text source power commands, menus, sidebars, and tablists.

## Current Default Style

### Commands

```kotlin
@DaisyCommandSet
object ProfileCommands : DaisyCommandGroup({
    command("profile") {
        description("Open your profile")

        player {
            replyLang("messages.profile.opening", "player" to player.name)
        }
    }
})
```

### Menus

```kotlin
player.openMenu(lang("menus.profile.title"), rows = 3) {
    background(Material.GRAY_STAINED_GLASS_PANE) {
        name(" ")
    }
}
```

### Scoreboards

```kotlin
sidebar {
    titleLang("sidebar.profile.title", viewer = player)
    line("coins") {
        textLang("sidebar.profile.coins", viewer = player, "coins" to "1,250")
    }
}
```

### Tablists

```kotlin
tablist {
    headerLang("tablist.profile.header", viewer = player, "player" to player.name)
    footerLang("tablist.profile.footer", viewer = player, "coins" to "1,250")
}
```

## What DaisyCore Means By “One Library”

You depend on `DaisyCore` once. Internally, the repo is still split into modules like `platform-command`, `platform-menu`, `platform-scoreboard`, and `platform-tablist` so the codebase stays maintainable.

Those internal module boundaries are an implementation detail. The user-facing story is:

- one platform bootstrap
- one shared text model
- one dependency
- one consistent Kotlin DSL style

## Current Runtime Truths

- Commands auto-load through runtime discovery today.
- Menus are now inline-first by default, though reusable menu definitions still exist.
- Sidebars are keyed and diff-friendly, with targeted invalidation as the main refresh pattern.
- Tablists are currently header/footer-focused. DaisyCore does not claim richer tab entry formatting yet.

## Example Plugin

The canonical repo example lives in [`example-plugin`](./example-plugin).

Start with:

- [`DaisyCoreExamplePlugin.kt`](./example-plugin/src/main/kotlin/cat/daisy/example/DaisyCoreExamplePlugin.kt)
- [`ProfileCommands.kt`](./example-plugin/src/main/kotlin/cat/daisy/example/ProfileCommands.kt)
- [`ExampleProfileUi.kt`](./example-plugin/src/main/kotlin/cat/daisy/example/ExampleProfileUi.kt)
- [`ExampleTextSource.kt`](./example-plugin/src/main/kotlin/cat/daisy/example/ExampleTextSource.kt)

## Docs And Migration

- Full docs: [docs.daisy.cat](https://docs.daisy.cat)
- Repo migration guide: [MIGRATION.md](./MIGRATION.md)
- Changelog: [CHANGELOG.md](./CHANGELOG.md)

## Contributor Verification

The repo has a broad CI task:

```bash
./gradlew quality
```

For local work, narrower runs are often faster and more reliable:

```bash
./gradlew.bat --no-daemon :example-plugin:compileKotlin
./gradlew.bat --no-daemon :platform-all:test --tests "cat.daisy.core.platform.DaisyPlatformBuilderTest"
```

When touching specific systems, prefer the module-level checks:

```bash
./gradlew.bat --no-daemon :platform-command:test --tests "cat.daisy.command.core.DaisyCommandsTest" --tests "cat.daisy.command.core.CommandAvailabilityTest"
./gradlew.bat --no-daemon :platform-menu:test --tests "cat.daisy.menu.DaisyMenuCoreTest"
./gradlew.bat --no-daemon :platform-scoreboard:test --tests "cat.daisy.scoreboard.DaisySidebarTest"
./gradlew.bat --no-daemon :platform-tablist:test --tests "cat.daisy.tablist.DaisyTablistTest"
```

The command test stack already sets the JVM self-attach flag through Gradle config, so contributors do not need to add it manually.

## Status

DaisyCore is a production-minded snapshot with the modern API direction already in place across commands, menus, text, scoreboards, and tablists. The current work is focused on product polish, migration clarity, and making the repo itself reflect the quality of the underlying API.

## Ecosystem Roadmap

Future Daisy-owned libraries such as DaisySeries are tracked in [DAISY_SUITE_ROADMAP.md](./DAISY_SUITE_ROADMAP.md).

## Related Projects

- [DaisySeries](https://github.com/DaisyCatTs/DaisySeries): lightweight parser utilities for materials, sounds, item flags, canonical keys, and friendly display names

## License

MIT. See [LICENSE](./LICENSE).
