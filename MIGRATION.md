# Migration

DaisyCore is the new home for the Daisy command, menu, text, sidebar, and tablist stack.

This file is the repo-level migration map for users coming from:

- old DaisyCommand usage
- old DaisyMenu usage
- plugin-local wrapper layers for MiniMessage, config text, and shared UI rendering

For deeper walkthroughs, use [docs.daisy.cat](https://docs.daisy.cat).

## Overview

The main shift is architectural:

- one platform owner through `DaisyPlatform`
- one shared text source through `messages(...)`
- one modern Kotlin DSL style across commands, menus, sidebars, and tablists

Instead of building separate plugin-local helper layers for each subsystem, DaisyCore now expects one shared setup:

```kotlin
daisy =
    DaisyPlatform.create(this) {
        messages(MyTextSource)
        commands()
        menus()
        scoreboards()
        tablists()
    }
```

## Commands

### Old shape

```kotlin
object MyCommands : DaisyCommandProvider {
    override fun commands(): List<DaisyCommand> = listOf(...)
}
```

### New default

```kotlin
@DaisyCommandSet
object MyCommands : DaisyCommandGroup({
    command("profile") {
        player {
            replyLang("messages.profile.opening")
        }
    }
})
```

### Main migrations

- `DaisyCommandProvider + commands(): List<DaisyCommand>` -> `DaisyCommandGroup`
- `executePlayer { ... }` -> `player { ... }`
- `executeConsole { ... }` -> `console { ... }`
- `getString(...) ?: return` -> `stringOr(...)` or `argOr(...)`
- plugin-local lang wrappers -> `replyLang(...)`, `lang(...)`, `loading()`, `failLang(...)`

### Compatibility note

Compatibility aliases still exist for several older patterns, but the documented default is now the cleaner `DaisyCommandGroup` + `player { ... }` style.

## Menus

### Old habit

- reusable menu definitions only
- plugin-local MiniMessage helpers
- filler panes and common click behavior repeated by hand

### New default

```kotlin
player.openMenu(lang("menus.profile.title"), rows = 3) {
    background(Material.GRAY_STAINED_GLASS_PANE) {
        name(" ")
    }

    slot(13) {
        item(Material.PLAYER_HEAD) {
            nameLang("menus.profile.card.name", viewer = player, "player" to player.name)
            loreLang("menus.profile.card.lore", viewer = player, "player" to player.name)
        }
        messageLang("messages.profile.clicked", "player" to player.name)
        closeOnClick()
    }
}
```

### Main migrations

- reusable-only menu style -> inline-first opening as the default path
- filler panes -> `background(...)`
- plugin-local MiniMessage helpers -> `nameLang(...)`, `loreLang(...)`, `messageLang(...)`
- repeated common click lambdas -> `closeOnClick()`, `refreshOnClick(...)`, `openUrl(...)`

### Important note

Reusable menu definitions still exist. The change is ergonomic priority, not feature removal.

## Sidebars

### New normal path

```kotlin
sidebar {
    titleLang("sidebar.profile.title", viewer = player)
    line("coins") {
        textLang("sidebar.profile.coins", viewer = player, "coins" to "1,250")
    }
}
```

### Main migrations

- plugin-local sidebar text wrappers -> shared `messages(...)`
- raw line render glue -> `line(key) { textLang(...) }`
- full redraw habit -> targeted `invalidate("coins")`

### Key behavior to keep in mind

- sidebars are keyed
- keyed lines are how diffing and invalidation work
- `invalidate(vararg keys)` is the normal targeted refresh path
- `refreshNow()` is still available for full recompute

## Tablists

### Current honest scope

Tablists are currently header/footer-focused.

### New normal path

```kotlin
tablist {
    headerLang("tablist.profile.header", viewer = player, "player" to player.name)
    footerLang("tablist.profile.footer", viewer = player, "coins" to "1,250")
}
```

### Main migrations

- plugin-local tablist text wrappers -> shared `messages(...)`
- manual header/footer formatting glue -> `headerLang(...)` and `footerLang(...)`
- ad-hoc refresh logic -> `refreshNow()`

### Scope note

DaisyCore does not currently claim richer tab entry formatting in the public API. Header and footer are the honest scope in this line.

## Shared Text

This is one of the biggest practical changes.

### Old habit

- config lookups in every subsystem
- repeated placeholder replacement helpers
- repeated MiniMessage parsing helpers

### New default

Install one `DaisyTextSource` once:

```kotlin
messages(MyTextSource)
```

Then use it everywhere:

- `replyLang(...)`
- `nameLang(...)`
- `loreLang(...)`
- `titleLang(...)`
- `textLang(...)`
- `headerLang(...)`
- `footerLang(...)`

## Final migration advice

Do not migrate subsystem by subsystem with new plugin-local wrappers.

The cleaner migration path is:

1. install `messages(...)`
2. move commands to `DaisyCommandGroup`
3. switch menus to the inline-first ergonomic path
4. move sidebars and tablists to lang-aware builders
5. delete plugin-local rendering glue that DaisyCore now handles directly
