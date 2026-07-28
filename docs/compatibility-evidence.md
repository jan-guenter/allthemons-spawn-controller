# Compatibility evidence

This document records the inputs inspected for the initial `0.1.0` target.
Third-party JARs are scratch evidence and are not committed or redistributed.

## Target lock

- All the Mons `1.1.1`, repository commit
  `94a224acf6eace3edf7ea64e6033b458f5bda288`
- Minecraft `1.21.1`
- NeoForge `21.1.234`
- Java `21`

The exact JAR ledger in the pack checkout identifies Aquaculture `2.7.21`,
Cobblemon `1.7.3+1.21.1`, Fight or Flight `0.10.9`, All The Mons core `0.3.0`,
InControl `1.21-10.2.6`, and KubeJS `2101.7.2-build.368`.

## NaturalSpawner 1.21.1 / NeoForge 21.1.234

ModDevGradle's generated source and compiled artifact were inspected. The
generated `sourcesWithNeoForge` archive had SHA-256
`455857e6f29d19286b437f535f841f0685656a475b9054522ab1050fd49491f5`;
the compiled NeoForge artifact had SHA-256
`37f7ca28c1a232e2b8b19bd1f4e2617f30cbe0dc0616ed771cc15c2e59b5f788`.
The required target is:

```text
NaturalSpawner.spawnForChunk(
    ServerLevel,
    LevelChunk,
    NaturalSpawner.SpawnState,
    boolean,
    boolean,
    boolean
)V
```

Its loop iterates the non-`MISC` `SPAWNING_CATEGORIES`, applies vanilla
friendly/persistent flags, invokes:

```text
NaturalSpawner$SpawnState.canSpawnForCategory(
    MobCategory,
    ChunkPos
)Z
```

and then invokes:

```text
NaturalSpawner.spawnCategoryForChunk(
    MobCategory,
    ServerLevel,
    LevelChunk,
    NaturalSpawner.SpawnPredicate,
    NaturalSpawner.AfterSpawnCallback
)V
```

The first wrap is the early policy gate. The second times the expensive
allowed pass. Both are required exactly once, and the Mixin configuration is
required with `defaultRequire: 1`.

## Aquaculture 2.7.21

Inspected JAR SHA-256:
`45f00f9059838b2fecc988861111d8b3d4613a5f1b3688a8dbfa8655751b85bb`.

`FishRegistry.lambda$register$3` supplies
`MobCategory.WATER_AMBIENT` to every normal `AquaFishEntity` builder.
`AquaEntities` supplies `MobCategory.CREATURE` for box, arrau, and starshell
turtles. Fish mounts, the bobber, and water/spectral-water arrows are `MISC`.

The biome modifiers place the registered fish/turtles into biome spawn lists;
they ultimately use the matching vanilla natural category pass. Bucket items
construct fish through a separate item path.

## Cobblemon 1.7.3

Inspected source tag `1.7.3`, commit
`e6fda6137e00cfe6035c863a3736ba30fc236696`, and JAR SHA-256
`962d75df4fb649d94863a7a7d130d4d2b3de4da9b3cae4c44b1ce90f37ec0ed5`.

`ServerPlayerMixin` calls its attached `PlayerSpawner.tick()` at the end of a
server player tick. `PlayerSpawner` counts down
`ticksBetweenSpawnAttempts`, creates a `SpawningZoneInput`, and invokes its
own area spawn pipeline. It does not call `NaturalSpawner.spawnForChunk`.

## Pack Mixin and rule audit

- Fight or Flight 0.10.9 JAR SHA-256:
  `9179ac75d11fb879a0f562a9d2ad43f3f4e3f21e26b23a8e3eaabc409c3024df`.
  Its required common Mixin list targets Pokémon/AI/combat classes, not
  `NaturalSpawner`.
- All The Mons core 0.3.0 JAR SHA-256:
  `35ed3ef101b9c0b9fbe5843c7a6a3f62b40748f2b62ea14fd49ca494102a879e`.
  Its Mixin list does not target `NaturalSpawner`.
- InControl 1.21-10.2.6 `ForgeEventHandlers` handles
  `MobSpawnEvent.PositionCheck`, `FinalizeSpawnEvent`, and
  `EntityJoinLevelEvent`; those are later than this category gate. The exact
  pack rule file denies `minecraft:squid`.

## Update warning

This is evidence for the exact versions above, not a compatibility promise for
future pack releases. Follow the complete procedure in `AGENTS.md` before
widening metadata or updating dependencies.
