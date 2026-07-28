package io.github.janguenter.allthemons.spawncontroller;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;

/**
 * Low-overhead category-loop counters. All timing values use monotonic nanoseconds.
 */
public final class SpawnDiagnostics {
    private static final ConcurrentHashMap<String, CategoryCounters> COUNTERS =
            new ConcurrentHashMap<>();

    private SpawnDiagnostics() {
    }

    public static void recordBlocked(String category) {
        CategoryCounters counters = counters(category);
        counters.policyChecks.increment();
        counters.policyBlocked.increment();
    }

    public static void recordAllowedCheck(String category, boolean capAllowed, long elapsedNanos) {
        CategoryCounters counters = counters(category);
        counters.policyChecks.increment();
        counters.capCheckNanos.add(elapsedNanos);
        counters.maxCapCheckNanos.accumulate(elapsedNanos);
        if (capAllowed) {
            counters.eligiblePasses.increment();
        } else {
            counters.capRejected.increment();
        }
    }

    public static void recordSpawnPass(String category, long elapsedNanos) {
        CategoryCounters counters = counters(category);
        counters.timedPasses.increment();
        counters.spawnPassNanos.add(elapsedNanos);
        counters.maxSpawnPassNanos.accumulate(elapsedNanos);
    }

    public static List<CategorySnapshot> snapshots() {
        return COUNTERS.entrySet().stream()
                .map(entry -> entry.getValue().snapshot(entry.getKey()))
                .sorted(Comparator.comparing(CategorySnapshot::category))
                .toList();
    }

    public static void reset() {
        COUNTERS.clear();
    }

    private static CategoryCounters counters(String category) {
        return COUNTERS.computeIfAbsent(category, ignored -> new CategoryCounters());
    }

    /**
     * Immutable command-facing diagnostics snapshot.
     */
    public record CategorySnapshot(
            String category,
            long policyChecks,
            long policyBlocked,
            long capRejected,
            long eligiblePasses,
            long timedPasses,
            long capCheckNanos,
            long maxCapCheckNanos,
            long spawnPassNanos,
            long maxSpawnPassNanos
    ) {
    }

    private static final class CategoryCounters {
        private final LongAdder policyChecks = new LongAdder();
        private final LongAdder policyBlocked = new LongAdder();
        private final LongAdder capRejected = new LongAdder();
        private final LongAdder eligiblePasses = new LongAdder();
        private final LongAdder timedPasses = new LongAdder();
        private final LongAdder capCheckNanos = new LongAdder();
        private final LongAccumulator maxCapCheckNanos = new LongAccumulator(Long::max, 0L);
        private final LongAdder spawnPassNanos = new LongAdder();
        private final LongAccumulator maxSpawnPassNanos = new LongAccumulator(Long::max, 0L);

        private CategorySnapshot snapshot(String category) {
            return new CategorySnapshot(
                    category,
                    policyChecks.sum(),
                    policyBlocked.sum(),
                    capRejected.sum(),
                    eligiblePasses.sum(),
                    timedPasses.sum(),
                    capCheckNanos.sum(),
                    maxCapCheckNanos.get(),
                    spawnPassNanos.sum(),
                    maxSpawnPassNanos.get()
            );
        }
    }
}
