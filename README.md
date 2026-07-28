# All the Mons Spawn Controller

A dedicated-server-only NeoForge add-on that stops selected vanilla natural-spawn
category passes before Minecraft chooses a random position in each chunk. It is
designed for an All the Mons server that normally runs Peaceful and temporarily
opens a 30-minute Easy voting window.

The mod registers no content, payloads, packet channels, screens, or client
hooks. Players do not install it.

## Compatibility

| Component | Inspected target |
| --- | --- |
| All the Mons | `1.1.1` |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.234` |
| Java | `21` |
| Aquaculture | `2.7.21` |
| Cobblemon | `1.7.3+1.21.1` |
| Fight or Flight | `0.10.9` |
| All The Mons core | `0.3.0` |
| InControl | `1.21-10.2.6` |
| KubeJS | `2101.7.2-build.368` |

The metadata deliberately requires Minecraft `[1.21.1,1.21.2)` and NeoForge
`[21.1.234,21.2)`. Treat another pack or platform version as unverified until
the update procedure in `AGENTS.md` has been completed.

## Policies

The policy is explicit; difficulty alone never selects it.

| Policy | NaturalSpawner category behavior |
| --- | --- |
| `peaceful` | Blocks every category in the vanilla natural-spawn loop. |
| `easy_vote` | Blocks configured categories; the default blocks `water_creature`, `water_ambient`, and `axolotls`, while allowing `monster`. |
| `stock` | Calls the original category-cap check and spawn pass for every category. Use for diagnosis and before/after comparison. |

The controller resets to `peaceful` at every server start. This is an
intentional fail-safe, not persistent state. The KubeJS vote script must
reconcile its persisted deadline and issue the matching policy command after
startup or script reload.

The policy does not change difficulty or `doMobSpawning`. The companion script
performs those actions atomically: Peaceful uses `doMobSpawning=false`, and an
active Easy vote uses `doMobSpawning=true`.

## Installation

1. Stop the dedicated server.
2. Copy the production JAR into the server's `mods` directory. Do not install
   it on clients.
3. Start once to generate
   `config/atmons_spawn_controller-common.toml`.
4. Install the companion vote script and confirm it reconciles the policy at
   server startup.
5. Run `atmons_spawn_controller status` from the server console.

Keep the existing InControl rules. They remain a later population-safety layer
for spawn paths that reach NeoForge's position/finalize/join events.

## Configuration

The common config contains one hot-path snapshot setting:

```toml
diagnosticsEnabled = true
easyVoteBlockedCategories = ["water_creature", "water_ambient", "axolotls"]
```

Names are serialized `MobCategory` names and are case-sensitive in the TOML
validator. Add `"creature"` to suppress ordinary land-passive natural spawning
during an Easy vote. Doing so also blocks all other entities using the
`CREATURE` category; it is not an Aquaculture-only switch. An empty list makes
`easy_vote` category behavior equivalent to stock while retaining the explicit
policy state.

Set `diagnosticsEnabled=false` after measurement to skip counter updates and
monotonic clock reads in the category loop. Previously collected counters
remain available until reset or restart.

The `peaceful` and `stock` definitions are fixed by design: `peaceful` blocks
the complete natural category loop, while `stock` bypasses all policy blocking.
A config reload updates an immutable runtime snapshot, so the per-chunk gate
does not parse the TOML list.

## Commands and KubeJS integration

The stable policy API is exactly:

```text
atmons_spawn_controller policy peaceful
atmons_spawn_controller policy easy_vote
atmons_spawn_controller policy stock
```

Changing policy requires permission level 3. KubeJS executes the companion
integration command from the server command source, which satisfies that
requirement. Status and diagnostics require permission level 2:

```text
atmons_spawn_controller status
atmons_spawn_controller diagnostics
atmons_spawn_controller diagnostics reset
```

`status` reports the current process-local policy and the Easy-vote blocked
category snapshot, including whether collection is enabled. When collection is
enabled, `diagnostics` reports, per category:

- policy checks and checks blocked before the vanilla cap call;
- cap-rejected and spawn-eligible checks;
- cumulative and maximum cap-check time;
- timed calls and cumulative/maximum time in
  `NaturalSpawner.spawnCategoryForChunk`.

These are category-pass measurements, not counts of entities spawned. Resetting
diagnostics requires permission level 3.

## Exact behavior and exclusions

The required Mixin wraps the
`NaturalSpawner.spawnForChunk(ServerLevel, LevelChunk, SpawnState, boolean,
boolean, boolean)` invocation of
`SpawnState.canSpawnForCategory(MobCategory, ChunkPos)`. A blocked policy
returns before the cap call and before `spawnCategoryForChunk`, random
position selection, biome spawn-list lookup, placement checks, entity
construction, and InControl's later spawn hooks. Allowed categories execute
the original calls unchanged. `NaturalSpawner.createState`, its existing-mob
census/cap data, and the vanilla friendly/persistent loop filters are left
unchanged.

Only the vanilla natural-spawn category loop is affected. The mod does not
hook entity construction or add/join events, so bucket release, breeding,
commands, spawn eggs, block/entity spawners, machines, and other direct or
special spawn mechanisms remain untouched. Chunk-generation creature spawning
uses `NaturalSpawner.spawnMobsForChunkGeneration`, which is not patched.

### Aquaculture

Aquaculture 2.7.21 bytecode was inspected:

- every `AquaFishEntity` registered through `FishRegistry` uses
  `WATER_AMBIENT`, so the default `easy_vote` policy blocks its natural fish
  pass;
- box, arrau, and starshell turtles use `CREATURE`, so they remain eligible by
  default and require adding `creature`;
- fishing mounts, arrows, and the bobber use `MISC` and are not in
  `NaturalSpawner`'s category loop.

Vanilla turtle natural spawning is also `CREATURE`. Vanilla squid and dolphins
use `WATER_CREATURE`; ordinary fish use `WATER_AMBIENT`; axolotls use
`AXOLOTLS`.

### Cobblemon and pack Mixins

Cobblemon 1.7.3 ticks its own per-player `PlayerSpawner`, builds a
`SpawningZoneInput`, and runs its separate spawn pool. This controller neither
targets that code nor classifies Pokémon by `MobCategory`, including aquatic
Pokémon.

The exact Fight or Flight 0.10.9 and All The Mons core 0.3.0 Mixin lists do not
target `NaturalSpawner`. InControl 1.21-10.2.6 evaluates NeoForge position,
finalize, and entity-join events later in the spawn lifecycle. The pack's
current `config/incontrol/spawn.json` squid denial remains useful in `stock`
mode and as a late guard.

More evidence and hashes are recorded in
[`docs/compatibility-evidence.md`](docs/compatibility-evidence.md).

## Trade-offs

- A blocked category cannot naturally replenish until policy or configuration
  allows it. Existing mobs are not removed.
- Blocking `creature` is broad and affects modded land passives sharing that
  category.
- `peaceful` intentionally blocks all natural category passes even if an
  administrator temporarily changes difficulty. Use `stock` for an intentional
  vanilla diagnostic window.
- With diagnostics enabled, timers add clock reads around allowed cap checks
  and cap-eligible category passes; blocked checks only increment counters.
  Disable collection after profiling to remove that diagnostic work.

## Verification and rollout

The repository gate is:

```bash
./gradlew --no-daemon clean check build
```

It runs pure JUnit policy/diagnostics tests, Checkstyle, and a production-JAR
layout check. A bare NeoForge dedicated-server smoke run is also required
before release because compilation cannot prove that the two exact Mixin
injection points still apply. Startup explicitly loads `NaturalSpawner` and
fails unless the required Mixin marker was merged; the required/allowed
injector counts then make either changed invocation fail transformation.

Before production rollout:

1. Back up the server and use a staging copy of the exact pack.
2. Verify startup reconciliation for an expired and an active vote deadline.
3. Verify hostile natural spawning during `easy_vote`.
4. Exercise Aquaculture fish/turtle behavior and every excluded spawn path.
5. Connect with an unmodified pack client.
6. Capture equivalent Spark profiles for `peaceful`, `easy_vote`, and `stock`.
   Compare median, p95/p99, maximum tick time, and named spawning paths.

The full All the Mons staging-world test, unmodified-client connection test,
and controlled Spark benchmark are not performed by this repository's unit
build and must not be inferred from a successful CI run.

## Rollback

Stop the server, remove this JAR, and restart. No required state is stored in
world data or entities; removing the mod restores stock behavior after that
restart. Also restore the vote script to stop issuing this mod's command.

## Releases and Maven

Tags are `v<mod_version>`. The release workflow rejects a mismatched tag,
repeats all checks, publishes the `mod` Maven publication to GitHub Packages,
creates SHA-256 checksums, and creates a GitHub Release.
Changes are recorded in [`CHANGELOG.md`](CHANGELOG.md).

```text
io.github.jan-guenter:allthemons-spawn-controller:0.1.0
```

Repository:

```text
https://maven.pkg.github.com/jan-guenter/allthemons-spawn-controller
```

GitHub Packages consumers need normal GitHub package credentials.

## License

The add-on code is available under the MIT License. Minecraft, NeoForge, and
the inspected pack mods retain their own licenses; none of their code or JARs
is redistributed here.
