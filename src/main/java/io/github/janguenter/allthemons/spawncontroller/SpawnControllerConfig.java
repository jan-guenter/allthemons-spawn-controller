package io.github.janguenter.allthemons.spawncontroller;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * Common server configuration and its allocation-free runtime snapshot.
 */
final class SpawnControllerConfig {
    private static final Pattern CATEGORY_NAME = Pattern.compile("[a-z0-9_.:/-]+");
    private static final ModConfigSpec SPEC;
    private static final ModConfigSpec.BooleanValue DIAGNOSTICS_ENABLED;
    private static final ModConfigSpec.ConfigValue<List<? extends String>> EASY_VOTE_BLOCKED;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        DIAGNOSTICS_ENABLED = builder
                .comment(
                        "Collect category-loop counters and monotonic timing.",
                        "Disable after profiling to remove diagnostic clock/counter overhead."
                )
                .define("diagnosticsEnabled", true);
        EASY_VOTE_BLOCKED = builder
                .comment(
                        "Serialized MobCategory names blocked in easy_vote.",
                        "Defaults keep monsters and land creatures while skipping aquatic categories.",
                        "Add \"creature\" to suppress ordinary passive and Aquaculture turtle passes."
                )
                .defineListAllowEmpty(
                        "easyVoteBlockedCategories",
                        new ArrayList<>(PolicyRules.DEFAULT_EASY_VOTE_BLOCKED_CATEGORIES),
                        SpawnControllerConfig::isCategoryName
                );
        SPEC = builder.build();
    }

    private SpawnControllerConfig() {
    }

    static void register(ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, SPEC);
        container.getEventBus().addListener((ModConfigEvent.Loading event) -> {
            if (event.getConfig().getSpec() == SPEC) {
                sync();
            }
        });
        container.getEventBus().addListener((ModConfigEvent.Reloading event) -> {
            if (event.getConfig().getSpec() == SPEC) {
                sync();
            }
        });
    }

    private static boolean isCategoryName(Object value) {
        return value instanceof String name && CATEGORY_NAME.matcher(name).matches();
    }

    private static void sync() {
        SpawnControllerState.installRules(new PolicyRules(EASY_VOTE_BLOCKED.get()));
        SpawnControllerState.setDiagnosticsEnabled(DIAGNOSTICS_ENABLED.get());
    }
}
