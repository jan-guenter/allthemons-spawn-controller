package io.github.janguenter.allthemons.spawncontroller.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.janguenter.allthemons.spawncontroller.NaturalSpawnerMixinMarker;
import io.github.janguenter.allthemons.spawncontroller.SpawnControllerState;
import io.github.janguenter.allthemons.spawncontroller.SpawnDiagnostics;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/**
 * Gates the exact vanilla category-loop cap check before positional spawning work.
 */
@Mixin(NaturalSpawner.class)
abstract class NaturalSpawnerMixin implements NaturalSpawnerMixinMarker {
    @WrapOperation(
            method = "spawnForChunk("
                    + "Lnet/minecraft/server/level/ServerLevel;"
                    + "Lnet/minecraft/world/level/chunk/LevelChunk;"
                    + "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;"
                            + "canSpawnForCategory("
                            + "Lnet/minecraft/world/entity/MobCategory;"
                            + "Lnet/minecraft/world/level/ChunkPos;)Z"
            ),
            require = 1,
            allow = 1
    )
    private static boolean atmonsSpawnController$gateCategory(
            NaturalSpawner.SpawnState spawnState,
            MobCategory category,
            ChunkPos chunkPos,
            Operation<Boolean> original
    ) {
        String categoryName = category.getSerializedName();
        if (SpawnControllerState.blocks(categoryName)) {
            if (SpawnControllerState.diagnosticsEnabled()) {
                SpawnDiagnostics.recordBlocked(categoryName);
            }
            return false;
        }

        if (!SpawnControllerState.diagnosticsEnabled()) {
            return original.call(spawnState, category, chunkPos);
        }

        long startNanos = System.nanoTime();
        boolean allowed = original.call(spawnState, category, chunkPos);
        SpawnDiagnostics.recordAllowedCheck(
                categoryName,
                allowed,
                System.nanoTime() - startNanos
        );
        return allowed;
    }

    @WrapOperation(
            method = "spawnForChunk("
                    + "Lnet/minecraft/server/level/ServerLevel;"
                    + "Lnet/minecraft/world/level/chunk/LevelChunk;"
                    + "Lnet/minecraft/world/level/NaturalSpawner$SpawnState;ZZZ)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/NaturalSpawner;"
                            + "spawnCategoryForChunk("
                            + "Lnet/minecraft/world/entity/MobCategory;"
                            + "Lnet/minecraft/server/level/ServerLevel;"
                            + "Lnet/minecraft/world/level/chunk/LevelChunk;"
                            + "Lnet/minecraft/world/level/NaturalSpawner$SpawnPredicate;"
                            + "Lnet/minecraft/world/level/NaturalSpawner$AfterSpawnCallback;)V"
            ),
            require = 1,
            allow = 1
    )
    private static void atmonsSpawnController$timeCategoryPass(
            MobCategory category,
            ServerLevel level,
            LevelChunk chunk,
            NaturalSpawner.SpawnPredicate predicate,
            NaturalSpawner.AfterSpawnCallback callback,
            Operation<Void> original
    ) {
        if (!SpawnControllerState.diagnosticsEnabled()) {
            original.call(category, level, chunk, predicate, callback);
            return;
        }

        long startNanos = System.nanoTime();
        try {
            original.call(category, level, chunk, predicate, callback);
        } finally {
            SpawnDiagnostics.recordSpawnPass(
                    category.getSerializedName(),
                    System.nanoTime() - startNanos
            );
        }
    }
}
