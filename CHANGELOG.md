# Changelog

All notable changes to DaisyCore are documented here.

## [Unreleased]

- Productization and release-surface polish continues across the repo, docs, and examples.

## [0.1.0-SNAPSHOT]

### Added

- One-library DaisyCore umbrella structure with internal platform modules.
- Runtime-owned `DaisyPlatform` bootstrap for commands, menus, scoreboards, tablists, and shared text.
- Runtime command discovery through annotated command sets.
- Shared `DaisyTextSource` installation through `messages(...)`.
- Inline-first menu opening and modern click/message helpers.
- Lang-aware sidebars with keyed invalidation.
- Header/footer-focused tablists with lang-aware rendering.
- Example plugin that demonstrates the modern profile flow across commands, menus, sidebars, and tablists.

### Changed

- Commands now prefer `DaisyCommandGroup`, `player { ... }`, `replyLang(...)`, and typed arg helpers.
- Menus now prefer inline opening, `background(...)`, and built-in lang-aware item/message helpers.
- Scoreboards now support cleaner line builders, `titleLang(...)`, and targeted keyed refresh ergonomics.
- Tablists now support `headerLang(...)` and `footerLang(...)` as the clean default rendering path.

### Docs

- `docs.daisy.cat` was expanded into the main docs product with beginner guides, reference pages, explanation pages, and premium UX polish.
- Migration and repo-facing documentation now reflect the modern DaisyCore API surface instead of older pre-ergonomic examples.

### Notes

- This is still a snapshot line, not a finalized stable release.
- Commands currently auto-load through runtime discovery.
- Tablists are intentionally scoped to header/footer behavior for now.
