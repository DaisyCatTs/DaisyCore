# Daisy Suite Roadmap

This roadmap covers the utility and ecosystem layer that should grow around DaisyCore after the core platform modules are stable.

## Goal

Build a Daisy-owned ecosystem of Kotlin-first Paper tooling that makes real server projects easier to write, safer to maintain, and more consistent across lifesteal, skyblock, prisons, survival, and future game modes.

## Order Of Attack

### Phase 1: Finish DaisyCore foundations

These need to be production-solid first because everything else should build on them:

- `platform-base`
- `platform-runtime`
- `platform-text`
- `platform-placeholders`
- `platform-items`
- `platform-scoreboard`
- `platform-tablist`
- refreshed `platform-command`
- refreshed `platform-menu`

### Phase 2: DaisySeries

Status:
- started as a separate repo and product in the Daisy ecosystem

This is the XSeries-style utility track.

Recommended starting packages:

- `cat.daisy.series.material`
- `cat.daisy.series.sound`
- `cat.daisy.series.itemflag`
- `cat.daisy.series.enchantment`
- `cat.daisy.series.potion`

Focus:

- clean Kotlin APIs
- modern-first support with carefully chosen compatibility helpers
- serialization-safe names
- “friendly name” helpers
- parser utilities for config input
- low-overhead caching

### Phase 3: DaisyConfig

Purpose:
typed config loading and validation for plugins using DaisyCore.

Targets:

- YAML + TOML or HOCON discussion later
- typed config mapping
- reload-safe config handles
- comment-friendly schema docs
- integration with DaisyText and DaisyItems

### Phase 4: DaisyData

Purpose:
data access and persistence utilities.

Targets:

- repository abstractions
- cache helpers
- async/sync safety
- player data lifecycle hooks
- codecs for common server-domain models

### Phase 5: DaisyEffects

Purpose:
particles, sounds, titles, action bars, and future display effects.

Targets:

- effect composition
- timed effect runners
- Adventure-first text hooks
- shared runtime scheduler integration

### Phase 6: DaisyWorld / DaisyPlayer utilities

Purpose:
frequently repeated gameplay helpers.

Targets:

- player state helpers
- teleport/safe-location helpers
- world utility helpers
- cooldown and status primitives reused by multiple plugins

### Phase 7: Higher-level server modules

Once the platform and utilities are mature, build server-facing libraries that solve bigger gameplay systems cleanly.

Examples:

- `DaisyEconomy`
- `DaisyCombat`
- `DaisyProfiles`
- `DaisyRegions`
- `DaisyLoot`

## Guardrails

- New suite modules must depend on DaisyCore foundations instead of reimplementing them.
- Do not add giant kitchen-sink helpers to `platform-base`.
- Keep packet-aware features optional.
- Prefer modern Paper-first quality over extreme legacy support.
- Only create a new library when it has a clear reusable boundary.

## Definition Of A Good Daisy Utility Library

- small public API
- Kotlin-first ergonomics
- excellent README and docs
- test coverage
- production-safe defaults
- useful for more than one of your actual server projects
