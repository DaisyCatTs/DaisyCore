# Daisy Ecosystem Vision

## Umbrella Goal

The Daisy ecosystem should become a coherent Kotlin-first toolkit for modern Paper development.

It should cover:

- platform/runtime primitives
- parsing and serialization helpers
- future config/data/effects layers
- higher-level server systems later

## Product Split

### DaisyCore

The runtime platform:

- bootstrap
- commands
- menus
- text/messages
- placeholders
- items
- sidebars
- tablists

### DaisySeries

The parser utility layer:

- config-friendly input parsing
- canonical keys
- display names
- curated aliases
- lightweight standalone adoption

### Future Daisy Libraries

Planned ecosystem directions:

- `DaisyConfig`
- `DaisyData`
- `DaisyEffects`
- `DaisyWorld`
- `DaisyPlayer`
- higher-level gameplay libraries after the foundations are stable

## What The End State Should Feel Like

A real plugin should be able to combine Daisy libraries cleanly:

```kotlin
val daisy =
    DaisyPlatform.create(this) {
        messages(MyTextSource)
        commands()
        menus()
        scoreboards()
        tablists()
    }

val icon = DaisyMaterials.parse(config.icon)
val enchantment = DaisyEnchantments.parse(config.enchantment)
```

The point is not “more libraries.”

The point is:

- clearer boundaries
- stronger defaults
- less plugin-local glue
- one consistent ecosystem style

## Quality Standard

Every Daisy-owned library should aim for:

- small public API
- Kotlin-first ergonomics
- strong docs
- migration clarity
- production-safe defaults
- narrow but honest scope

## Ecosystem Guardrails

- Do not bloat DaisyCore with unrelated utility families.
- Keep DaisySeries usable without DaisyCore.
- Keep docs under one Daisy brand surface.
- Prefer honest runtime truth over bigger marketing claims.
- Only split a new library when the boundary is actually reusable.

## Docs Strategy

`docs.daisy.cat` should stay the shared product surface for:

- DaisyCore
- DaisySeries
- future Daisy libraries when they become real

The docs root should help users choose the right product quickly, not force everything into one monolith.

## Long-Term Outcome

The final Daisy ecosystem should let you build a Paper plugin stack with:

- one platform layer
- one parser/utility layer
- one docs surface
- one design language
- one clear product story

Without needing to re-solve the same engineering problems in every server repo.
