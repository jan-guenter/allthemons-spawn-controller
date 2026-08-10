# Compatibility evidence

This document records the inputs inspected for the `0.1.1` compatibility
update. Third-party JARs are scratch evidence and are not committed or
redistributed.

## Target lock

- All the Mons `1.2.0`, repository commit
  `c7bb230f21d14d26859d0b92548f089b3a493ad9`
- CurseForge export file `8572588`, SHA-256
  `1d37df201daddecf5454115f5205cca15ca6ab84ed102bcc2e312f4c14876e5d`
- CurseForge server archive file `8572602`, SHA-256
  `de112ed8d79b3ff027e399a5108b706f6a2db3be74b15d0db6f6b9d6ac268e6c`
- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Java `21`

The exact export manifest contains 402 files. Compared with 1.1.1, it retains
the same CurseForge project/file pairs for the dependencies that define this
controller's behavior:

| Mod | Project/file ID | Version |
| --- | --- | --- |
| Aquaculture | `60028/7901588` | `2.7.21` |
| Cobblemon | `687131/7553231` | `1.7.3+1.21.1` |
| Fight or Flight | `1168140/8404745` | `0.10.9` |
| InControl | `257356/7436215` | `1.21-10.2.6` |
| KubeJS | `238086/8083208` | `2101.7.2-build.368` |

All The Mons core changed from `0.3.0` file `8427273` to `0.5.0` file
`8561730`. ModernFix changed from `5.27.15` file `8360807` to `5.27.20`
file `8459650`, and the pack enabled its beta-stability option.

Primary release inputs:

- [All the Mons 1.2.0 pack changelog](https://github.com/AllTheMods/All-the-Mons/blob/1.2.0/changelogs/CHANGELOG-All%20The%20Mons-1.1.1-1.2.0.md)
- [All The Mons core 0.5.0 file and changelog](https://www.curseforge.com/minecraft/mc-mods/all-the-mons-coremod/files/8561730)
- [ModernFix 5.27.20 file](https://www.curseforge.com/minecraft/mc-mods/modernfix/files/8459650)
- [The Bumblezone 7.15.3 file and changelog](https://www.curseforge.com/minecraft/mc-mods/the-bumblezone-forge/files/8548224)
- [NeoForge 21.1.248 cumulative changelog](https://maven.neoforged.net/releases/net/neoforged/neoforge/21.1.248/neoforge-21.1.248-changelog.txt)

## NaturalSpawner 1.21.1 / NeoForge 21.1.248

NeoForge's official changelog entries from `21.1.235` through `21.1.248` do
not change natural spawning or the `NaturalSpawner` class. The update includes
unrelated fixes, FancyModLoader `4.0.43`, ASM `9.10.1`, and registry/data-map
changes. Its downloaded changelog had SHA-256
`802eaf61ffd23bd57ecf53dc21e0c83fa800aae2af8795634bba6f9e6b96ae2f`;
the official universal JAR had SHA-256
`90a56f70425711b4e1a4b94ff0c2904ae9f6d74ca6478b3b2152ac794a07b8e5`.

ModDevGradle generated the new patched Minecraft source and compiled
artifacts. The generated `sourcesWithNeoForge` archive had SHA-256
`d9803fd6cd64337cf0b27059047df2ed000a07ba303200ff858d38674d9e38e8`;
the compiled artifact had SHA-256
`ccf859e1458fc2c820a665a96c74dc4344c52c864856366d70dfecdc439e3e5b`.
The complete generated `NaturalSpawner.java` file is byte-for-byte identical
to the one produced for NeoForge 21.1.234.

The required target remains:

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

Its loop still iterates the non-`MISC` `SPAWNING_CATEGORIES`, applies vanilla
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

The first wrap remains the early policy gate and the second remains the timing
wrapper around the expensive allowed pass. Both are required exactly once,
and the Mixin configuration remains required with `defaultRequire: 1`.

## All The Mons core 0.5.0 and ModernFix 5.27.20

The exact `allthemons-0.5.0.jar` had SHA-256
`b5ae8fa0d0e16d104e79d71b085dc64f3f00b845cd5f1dba21f91e7a686683c6`.
Its required and client Mixin lists contain no target named
`NaturalSpawner`, and no class in the unpacked JAR references that class.

The new coremod Mixins add pasture/Pika Power handling and Create rotation
propagation. The existing `MixinCobblemonBiomeModifiers` class is
byte-for-byte identical to coremod 0.3.0
(class SHA-256
`31b2656843835abca5b8ef379c37af8d6f2c3d51c1fea7a650ec8bd1ab6f091c`).
The new Mixin-canceller plugin matches only
`com.jorgaomc.legendarymonuments.mixin.IngredientMixin`; it cannot match this
controller's Mixin name.

The exact ModernFix 5.27.20 JAR had SHA-256
`6f367ea4002c39e3cfe27e0a0be1008880a4b35a8482a78a437cfb9d8ee38273`.
Neither its packaged Mixin classes nor configuration references
`NaturalSpawner`. The pack's only ModernFix config change is
`stability_level=BETA`; the already-enabled
`mixin.perf.dynamic_resources=true` line is unchanged. These options do not
overlap the controller's target.

## Whole-pack NaturalSpawner Mixin scan

All 375 top-level mod JARs in the exact 1.2.0 server archive were scanned,
including embedded JARs, for `NaturalSpawner` references. Two installed mods
contain Mixins targeting that class:

- Artifacts 13.2.1 injects after entity insertion inside the six-argument
  `spawnCategoryForPosition` method to equip configured accessory loot.
- The Bumblezone injects at the head of that same
  `spawnCategoryForPosition` method and cancels out-of-height spawning only in
  its own dimension.

Neither patches `spawnForChunk`, `SpawnState.canSpawnForCategory`, or
`spawnCategoryForChunk`, so neither overlaps either exact invocation wrapped
by this controller. A blocked policy prevents the lower-level method from
being reached; an allowed policy calls vanilla once and preserves both
downstream Mixins.

Artifacts is unchanged at CurseForge project/file `312353/7875426`. The
Bumblezone changed from 7.15.1 file `8369659` to 7.15.3 file `8548224`, but its
`MobSpawnLocationMixin.class` is byte-for-byte identical in both JARs
(SHA-256
`85b7bab9c74fdfffc895c3197742d867a14dd1fb1044bfdbb5c1eb0725be9d30`).
The official 7.15.2 and 7.15.3 changes concern the Crystalline Flower UI,
fluid collision/waterlogging, and localization rather than spawning.

The scan also found ordinary `NaturalSpawner` callers or type references in
Apotheosis, Allthemodium, and Eternal Starlight. They are not Mixins and do not
modify either controller injection point.

## Unchanged spawning dependencies

The unchanged file IDs preserve the binary evidence from the 0.1.0 audit:

- Aquaculture 2.7.21 JAR SHA-256:
  `45f00f9059838b2fecc988861111d8b3d4613a5f1b3688a8dbfa8655751b85bb`.
  `FishRegistry.lambda$register$3` assigns `WATER_AMBIENT` to normal fish;
  box, arrau, and starshell turtles are `CREATURE`; mounts, arrows, and the
  bobber are `MISC`.
- Cobblemon source tag `1.7.3`, commit
  `e6fda6137e00cfe6035c863a3736ba30fc236696`, and JAR SHA-256
  `962d75df4fb649d94863a7a7d130d4d2b3de4da9b3cae4c44b1ce90f37ec0ed5`.
  `ServerPlayerMixin` still ticks its attached `PlayerSpawner`, which runs a
  separate `SpawningZoneInput` pipeline rather than
  `NaturalSpawner.spawnForChunk`.
- Fight or Flight 0.10.9 JAR SHA-256:
  `9179ac75d11fb879a0f562a9d2ad43f3f4e3f21e26b23a8e3eaabc409c3024df`.
  Its common Mixin list does not target `NaturalSpawner`.
- InControl 1.21-10.2.6 still handles NeoForge position, finalize, and entity
  join events after the controller's early category gate.

The exact 1.1.1 and 1.2.0 overrides have identical hashes for the relevant
pack configuration:

| File | SHA-256 |
| --- | --- |
| `config/incontrol/spawn.json` | `c3f31ed1f9f7cc4ba535144c9d2e5a59cc8aaee6531afe1e36dd244bda6f7afb` |
| `config/cobblemon/main.json` | `eb07435d618a735f6d5f3d1ea9d51414d80934eb41bae41ba3d2d84c516af11d` |
| `config/fightorflight_visual_effect.json5` | `d878610a175441b5d22ea73f0dbc19a89204b5c207fb9af83c288598a5980056` |

The pack's changed KubeJS startup/server scripts add Cobblemon medicine
brewing, block strippables, trainer migration, and recipe/tag changes; none
sets difficulty, `doMobSpawning`, or this controller's policy. The unchanged
KubeJS file ID also preserves the companion command bridge API.

The separate companion script was syntax-checked and its Node test suite was
run. It still issues exactly:

```text
atmons_spawn_controller policy peaceful
atmons_spawn_controller policy easy_vote
```

alongside the matching difficulty and `doMobSpawning` changes. No controller
command or state-contract change is required for 1.2.0.

## Runtime verification and boundary

The repository build and bare NeoForge dedicated-server smoke proved the exact
two required injection points apply on NeoForge 21.1.248 and the stable command
API executes.

The release candidate was also installed into a fresh extraction of the exact
All the Mons 1.2.0 server archive identified above. The complete 375-mod server
reached `Done`, logged the required runtime-loaded `NaturalSpawner` target,
accepted `easy_vote`, `stock`, and `peaceful` policy transitions, reported the
expected status after the first and last transitions, and shut down cleanly.
The log contained no Mixin application or injection failure for this mod.

This smoke does not constitute an unmodified-client connection test, extended
gameplay in an existing world, or a controlled Spark comparison. Those remain
rollout checks for the operator.

## Update warning

This evidence applies only to the exact versions and artifacts above. Follow
the complete procedure in `AGENTS.md` before widening metadata or updating
dependencies again.
