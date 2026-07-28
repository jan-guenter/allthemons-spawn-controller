# Changelog

All notable changes to this project are documented here. The project follows
Semantic Versioning.

## [Unreleased]

## [0.1.0] - 2026-07-28

### Added

- Dedicated-server-only `peaceful`, `easy_vote`, and `stock` natural-spawn
  policies.
- Early configured `MobCategory` gate in the exact Minecraft 1.21.1 /
  NeoForge 21.1.234 `NaturalSpawner` category loop.
- Stable `atmons_spawn_controller` policy, status, and diagnostics commands.
- Fail-safe Peaceful startup state for KubeJS deadline reconciliation.
- Per-category cap/pass counters and monotonic timing diagnostics.
- Pure policy and diagnostics tests, strict runtime Mixin marker verification,
  CI/release automation, Maven publication, and operator/update documentation.

[Unreleased]: https://github.com/jan-guenter/allthemons-spawn-controller/compare/v0.1.0...HEAD
[0.1.0]: https://github.com/jan-guenter/allthemons-spawn-controller/releases/tag/v0.1.0
