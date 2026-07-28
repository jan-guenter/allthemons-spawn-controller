package io.github.janguenter.allthemons.spawncontroller;

import net.minecraft.world.level.NaturalSpawner;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Dedicated-server entry point. No content, payload, or client registration occurs here.
 */
@Mod(value = AllTheMonsSpawnController.MOD_ID, dist = Dist.DEDICATED_SERVER)
public final class AllTheMonsSpawnController {
    public static final String MOD_ID = "atmons_spawn_controller";
    private static final Logger LOGGER = LoggerFactory.getLogger("AtMonsSpawnController");

    public AllTheMonsSpawnController(ModContainer container) {
        SpawnControllerConfig.register(container);
        NeoForge.EVENT_BUS.addListener(SpawnControllerCommands::register);
        NeoForge.EVENT_BUS.addListener(this::onServerStarting);
    }

    private void onServerStarting(ServerStartingEvent event) {
        Class<?> targetClass = NaturalSpawner.class;
        if (!NaturalSpawnerMixinMarker.class.isAssignableFrom(targetClass)) {
            throw new IllegalStateException(
                    "Required NaturalSpawner Mixin did not apply"
            );
        }
        LOGGER.info("Runtime-loaded required Mixin target {}", targetClass.getName());
        SpawnControllerState.setPolicy(SpawnPolicy.PEACEFUL, "server startup fail-safe");
        SpawnDiagnostics.reset();
    }
}
