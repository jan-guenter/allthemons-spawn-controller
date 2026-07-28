package io.github.janguenter.allthemons.spawncontroller;

import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Small process-local state holder. Startup intentionally resets the policy to peaceful.
 */
public final class SpawnControllerState {
    private static final Logger LOGGER = LoggerFactory.getLogger("AtMonsSpawnController");
    private static volatile SpawnPolicy policy = SpawnPolicy.PEACEFUL;
    private static volatile PolicyRules rules = PolicyRules.defaults();
    private static volatile boolean diagnosticsEnabled = true;

    private SpawnControllerState() {
    }

    public static SpawnPolicy policy() {
        return policy;
    }

    public static Set<String> easyVoteBlockedCategories() {
        return rules.easyVoteBlockedCategories();
    }

    public static boolean blocks(String serializedCategoryName) {
        return rules.blocks(policy, serializedCategoryName);
    }

    public static boolean diagnosticsEnabled() {
        return diagnosticsEnabled;
    }

    public static void setPolicy(SpawnPolicy nextPolicy, String source) {
        SpawnPolicy previous = policy;
        policy = Objects.requireNonNull(nextPolicy, "nextPolicy");
        LOGGER.info("Spawn policy changed from {} to {} by {}",
                previous.commandName(), nextPolicy.commandName(), source);
    }

    static void installRules(PolicyRules nextRules) {
        rules = Objects.requireNonNull(nextRules, "nextRules");
        LOGGER.info("Easy-vote blocked natural-spawn categories: {}",
                nextRules.easyVoteBlockedCategories());
    }

    static void setDiagnosticsEnabled(boolean enabled) {
        diagnosticsEnabled = enabled;
        LOGGER.info("Natural-spawn category diagnostics enabled: {}", enabled);
    }
}
