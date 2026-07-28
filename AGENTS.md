# Agent guide

## Mission and invariants

This repository is the dedicated-server-only natural-spawn policy controller
for All the Mons. Preserve these invariants:

- no blocks, items, entities, menus, recipes, payloads, packet channels,
  screens, or client hooks;
- no required state in worlds, entities, or another mod's data;
- startup always fails safe to `peaceful` until KubeJS reconciles;
- `stock` must call the original vanilla category checks and spawn passes;
- `NaturalSpawner.createState` and existing-mob cap census remain untouched;
- bucket, breeding, command, spawn-egg, chunk-generation, block-spawner,
  machine, and other direct/special spawn paths remain unhooked;
- Cobblemon's separate player-spawner pipeline remains unhooked;
- removing the JAR and restarting restores stock behavior without conversion;
- never claim a full-pack startup, unmodified-client connection, production
  world test, or Spark result unless it was actually run.

## Locked target

| Component | Version |
| --- | --- |
| All the Mons | `1.1.1` |
| Pack commit | `94a224acf6eace3edf7ea64e6033b458f5bda288` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.234` |
| Java | `21` |
| Aquaculture | `2.7.21` |
| Cobblemon | `1.7.3+1.21.1` |
| Fight or Flight | `0.10.9` |
| All The Mons core | `0.3.0` |
| InControl | `1.21-10.2.6` |
| KubeJS | `2101.7.2-build.368` |

Hard dependency ranges are Minecraft `[1.21.1,1.21.2)` and NeoForge
`[21.1.234,21.2)`. Do not widen them based only on compilation.

## Exact Mixin contract

Target class:
`net.minecraft.world.level.NaturalSpawner`.

Target method descriptor:

```text
spawnForChunk(
  Lnet/minecraft/server/level/ServerLevel;
  Lnet/minecraft/world/level/chunk/LevelChunk;
  Lnet/minecraft/world/level/NaturalSpawner$SpawnState;
  ZZZ
)V
```

Required invocation 1:

```text
Lnet/minecraft/world/level/NaturalSpawner$SpawnState;
canSpawnForCategory(
  Lnet/minecraft/world/entity/MobCategory;
  Lnet/minecraft/world/level/ChunkPos;
)Z
```

Required invocation 2:

```text
Lnet/minecraft/world/level/NaturalSpawner;
spawnCategoryForChunk(
  Lnet/minecraft/world/entity/MobCategory;
  Lnet/minecraft/server/level/ServerLevel;
  Lnet/minecraft/world/level/chunk/LevelChunk;
  Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;
  Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;
)V
```

`NaturalSpawnerMixin` is listed only in the Mixin config's physical `server`
array and uses required MixinExtras `@WrapOperation` injections.
Keep the explicit `[[mixins]]` registration in `neoforge.mods.toml` as well as
the JAR manifest `MixinConfigs` attribute; development server runs do not
discover the configuration from the eventual production-JAR manifest.
The policy gate wraps invocation 1 before cap/position work. For an allowed
category, always call `original` exactly once. The timing wrapper calls
invocation 2 exactly once in a `try/finally`. The Mixin JSON is required, Java
21, and `defaultRequire` is 1; both wrappers set `require=1` and `allow=1`.
Server startup loads the target and verifies `NaturalSpawnerMixinMarker` was
merged. A runtime dedicated-server smoke is mandatory because compilation
cannot prove the invocation targets still exist.

Do not broaden the injection to `spawnCategoryForPosition`; doing so would
affect debug/direct callers and lose the category-loop-only exclusion.
Do not hook entity creation or join events; those would affect the explicitly
excluded spawn mechanisms and would execute too late for the performance goal.

## Policy and command contract

The public automation command must remain exactly:

```text
atmons_spawn_controller policy <peaceful|easy_vote|stock>
```

Policy changes require permission level 3. The companion KubeJS script uses
the server command source and is authorized. `status` and `diagnostics` require
level 2; diagnostics reset requires level 3.

`peaceful` blocks every category reaching the natural loop. `easy_vote` uses
the immutable config snapshot and defaults to `water_creature`,
`water_ambient`, and `axolotls`; `monster` must remain allowed by default.
`creature` is intentionally optional. `stock` never blocks.
`diagnosticsEnabled=false` must skip timing/counter work without changing any
policy result; retained snapshots are cleared only by reset or startup.

The mod does not own difficulty or `doMobSpawning`. KubeJS persists the vote
deadline, sets policy plus difficulty plus the gamerule, and reconciles after
startup/reload. Never add an in-memory timer here.

## Pack interaction evidence

- Aquaculture `FishRegistry.lambda$register$3` registers all normal fish as
  `WATER_AMBIENT`.
- Aquaculture `AquaEntities` registers box, arrau, and starshell turtles as
  `CREATURE`; mounts/arrows/bobber are `MISC`.
- Adding `creature` blocks those turtles but also every vanilla/modded land
  passive in that category.
- Cobblemon commit `e6fda6137e00cfe6035c863a3736ba30fc236696`
  ticks `PlayerSpawner` from `ServerPlayerMixin` and uses
  `SpawningZoneInput`, separately from `NaturalSpawner`.
- Fight or Flight 0.10.9 and All The Mons core 0.3.0 required Mixin lists do
  not target `NaturalSpawner`.
- InControl 1.21-10.2.6 handles NeoForge position/finalize/join events after
  the early gate. Keep it as a late population guard. The locked pack rule
  denies `minecraft:squid`.

Hashes and detailed paths are in `docs/compatibility-evidence.md`.

## Build and verification

Use Java 21 and the checked-in Gradle 8.10.2 wrapper:

```bash
./gradlew --no-daemon clean check build
```

This must pass JUnit 5 pure policy/diagnostics tests, Checkstyle, and
`verifyProductionJar`. Inspect the final non-sources JAR:

```bash
jar tf build/libs/allthemons-spawn-controller-<version>.jar
```

It must contain project classes, `META-INF/neoforge.mods.toml`, the required
Mixin JSON, manifest/license, and project resources only. It must not contain
third-party JARs, client classes/hooks, or content assets.

Run the configured bare dedicated-server smoke and confirm both
`@WrapOperation` injections apply. Then use the exact pack in staging for
policy behavior, excluded spawn paths, KubeJS restart reconciliation, and an
unmodified-client connection. Run equivalent Spark profiles separately.

## Updating to a new All the Mons release

Treat every pack update as a fresh compatibility investigation:

1. Check out the exact new pack tag and record Minecraft, NeoForge, and every
   relevant dependency JAR name, mod ID, version, hash, and source tag.
2. Diff pack configs, KubeJS, datapacks, and the complete mod list.
3. Obtain the exact new binary JARs. Inspect `neoforge.mods.toml`, Mixin
   configurations, target bytecode, and all implementations/callers of the
   patched APIs.
4. Re-check `NaturalSpawner`, Aquaculture category registration and biome
   modifiers, InControl handlers/rules, Fight or Flight Mixins, All The Mons
   core Mixins, and Cobblemon's spawning pipeline. Scan every installed JAR
   for another Mixin targeting `NaturalSpawner`.
5. Update compile/runtime dependencies and strict metadata ranges only after
   source and bytecode review.
6. Make changed signatures fail in tests/Mixin application rather than adding
   a broad compatibility range. Update both exact descriptors above.
7. Run unit tests, the required runtime Mixin smoke/game test, a dedicated
   server startup, and an unmodified-client connection test on a staging copy.
8. Repeat controlled before/after Spark profiles with equivalent world,
   players, chunks, duration, and activity. Record median, p95/p99, maximum,
   and named hot paths.
9. Update `README.md`, this compatibility table, evidence, and
   changelog/release notes; increase `mod_version`; merge; then create the
   matching `v*` tag.

For Aquaculture, enumerate every registered entity category again; never infer
category from an entity looking aquatic. For Cobblemon, prove the player
spawner remains separate. For InControl, distinguish position/finalize/join
guards from early category gating.

## Release procedure

1. Ensure the worktree is clean and `main` is current.
2. Increase semantic `mod_version` in `gradle.properties`.
3. Update documentation/release notes and run the complete gate plus runtime
   smoke.
4. Merge through a pull request; the version-policy workflow requires an
   increase over the base branch.
5. Create and push tag `v<mod_version>`.
6. Confirm the release workflow repeats checks, publishes
   `io.github.jan-guenter:allthemons-spawn-controller:<version>` to GitHub
   Packages, uploads only the production JAR plus checksums to the GitHub
   Release, and does not publish a third-party binary.

Never manually publish a tag whose value differs from `mod_version`.
