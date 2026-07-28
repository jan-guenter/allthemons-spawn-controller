package io.github.janguenter.allthemons.spawncontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SpawnDiagnosticsTest {
    @AfterEach
    void cleanUp() {
        SpawnDiagnostics.reset();
    }

    @Test
    void recordsIndependentPerCategoryCountersAndTiming() {
        SpawnDiagnostics.recordBlocked("water_creature");
        SpawnDiagnostics.recordAllowedCheck("monster", false, 12L);
        SpawnDiagnostics.recordAllowedCheck("monster", true, 18L);
        SpawnDiagnostics.recordSpawnPass("monster", 30L);

        var snapshots = SpawnDiagnostics.snapshots();
        assertEquals(2, snapshots.size());

        var monster = snapshots.stream()
                .filter(snapshot -> snapshot.category().equals("monster"))
                .findFirst()
                .orElseThrow();
        assertEquals(2L, monster.policyChecks());
        assertEquals(0L, monster.policyBlocked());
        assertEquals(1L, monster.capRejected());
        assertEquals(1L, monster.eligiblePasses());
        assertEquals(1L, monster.timedPasses());
        assertEquals(30L, monster.capCheckNanos());
        assertEquals(18L, monster.maxCapCheckNanos());
        assertEquals(30L, monster.spawnPassNanos());
        assertEquals(30L, monster.maxSpawnPassNanos());
    }

    @Test
    void resetDropsAllSnapshots() {
        SpawnDiagnostics.recordBlocked("axolotls");
        SpawnDiagnostics.reset();

        assertTrue(SpawnDiagnostics.snapshots().isEmpty());
    }
}
