package io.github.janguenter.allthemons.spawncontroller;

import com.mojang.brigadier.CommandDispatcher;
import java.util.Locale;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

/**
 * Stable operator and KubeJS command surface.
 */
final class SpawnControllerCommands {
    static final String ROOT = "atmons_spawn_controller";

    private SpawnControllerCommands() {
    }

    static void register(RegisterCommandsEvent event) {
        registerDispatcher(event.getDispatcher());
    }

    private static void registerDispatcher(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(ROOT)
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("policy")
                        .requires(source -> source.hasPermission(3))
                        .then(policyLiteral(SpawnPolicy.PEACEFUL))
                        .then(policyLiteral(SpawnPolicy.EASY_VOTE))
                        .then(policyLiteral(SpawnPolicy.STOCK)))
                .then(Commands.literal("status")
                        .executes(context -> reportStatus(context.getSource())))
                .then(Commands.literal("diagnostics")
                        .executes(context -> reportDiagnostics(context.getSource()))
                        .then(Commands.literal("reset")
                                .requires(source -> source.hasPermission(3))
                                .executes(context -> resetDiagnostics(context.getSource())))));
    }

    private static com.mojang.brigadier.builder.LiteralArgumentBuilder<CommandSourceStack>
            policyLiteral(SpawnPolicy policy) {
        return Commands.literal(policy.commandName())
                .executes(context -> setPolicy(context.getSource(), policy));
    }

    private static int setPolicy(CommandSourceStack source, SpawnPolicy policy) {
        SpawnControllerState.setPolicy(policy, source.getTextName());
        source.sendSuccess(
                () -> Component.literal("All the Mons spawn policy is now "
                        + policy.commandName()),
                true
        );
        return 1;
    }

    private static int reportStatus(CommandSourceStack source) {
        source.sendSuccess(
                () -> Component.literal("policy=" + SpawnControllerState.policy().commandName()
                        + " easyVoteBlockedCategories="
                        + SpawnControllerState.easyVoteBlockedCategories()
                        + " diagnosticsEnabled="
                        + SpawnControllerState.diagnosticsEnabled()),
                false
        );
        return 1;
    }

    private static int reportDiagnostics(CommandSourceStack source) {
        var snapshots = SpawnDiagnostics.snapshots();
        if (snapshots.isEmpty()) {
            source.sendSuccess(
                    () -> Component.literal("No natural-spawn category checks recorded"),
                    false
            );
            return 1;
        }

        for (SpawnDiagnostics.CategorySnapshot snapshot : snapshots) {
            String line = String.format(
                    Locale.ROOT,
                    "%s checks=%d blocked=%d capRejected=%d eligible=%d timedPasses=%d "
                            + "capMs=%.3f capMaxMs=%.3f passMs=%.3f passMaxMs=%.3f",
                    snapshot.category(),
                    snapshot.policyChecks(),
                    snapshot.policyBlocked(),
                    snapshot.capRejected(),
                    snapshot.eligiblePasses(),
                    snapshot.timedPasses(),
                    nanosToMillis(snapshot.capCheckNanos()),
                    nanosToMillis(snapshot.maxCapCheckNanos()),
                    nanosToMillis(snapshot.spawnPassNanos()),
                    nanosToMillis(snapshot.maxSpawnPassNanos())
            );
            source.sendSuccess(() -> Component.literal(line), false);
        }
        return snapshots.size();
    }

    private static int resetDiagnostics(CommandSourceStack source) {
        SpawnDiagnostics.reset();
        source.sendSuccess(
                () -> Component.literal("Natural-spawn diagnostics reset"),
                false
        );
        return 1;
    }

    private static double nanosToMillis(long nanoseconds) {
        return nanoseconds / 1_000_000.0D;
    }
}
