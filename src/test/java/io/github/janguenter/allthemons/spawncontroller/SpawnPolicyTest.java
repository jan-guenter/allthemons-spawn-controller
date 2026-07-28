package io.github.janguenter.allthemons.spawncontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class SpawnPolicyTest {
    @Test
    void commandNamesAreStableAndExact() {
        assertEquals(
                SpawnPolicy.PEACEFUL,
                SpawnPolicy.fromCommandName("peaceful").orElseThrow()
        );
        assertEquals(
                SpawnPolicy.EASY_VOTE,
                SpawnPolicy.fromCommandName("easy_vote").orElseThrow()
        );
        assertEquals(
                SpawnPolicy.STOCK,
                SpawnPolicy.fromCommandName("stock").orElseThrow()
        );
        assertTrue(SpawnPolicy.fromCommandName("easy").isEmpty());
        assertTrue(SpawnPolicy.fromCommandName("EASY_VOTE").isEmpty());
    }
}
