# Changelog

All notable changes to this project are documented here. The project follows
Semantic Versioning.

## [Unreleased]

## [0.1.1] - 2026-08-10

### Changed

- Retargeted the exact All the Mons 1.2.0 / NeoForge 21.1.248 runtime while
  retaining the unchanged Minecraft 1.21.1 `NaturalSpawner` injection
  contract and companion KubeJS command API.
- Re-audited the updated All The Mons coremod 0.5.0 and unchanged Aquaculture,
  Cobblemon, Fight or Flight, InControl, KubeJS, and relevant pack configs.
- Raised the NeoForge metadata floor to 21.1.248 and refreshed operator and
  compatibility evidence.
- Validated all policy transitions in a fresh dedicated-server startup of the
  exact All the Mons 1.2.0 server archive.

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

[Unreleased]: https://github.com/jan-guenter/allthemons-spawn-controller/compare/v0.1.1...HEAD
[0.1.1]: https://github.com/jan-guenter/allthemons-spawn-controller/compare/v0.1.0...v0.1.1
[0.1.0]: https://github.com/jan-guenter/allthemons-spawn-controller/releases/tag/v0.1.0
