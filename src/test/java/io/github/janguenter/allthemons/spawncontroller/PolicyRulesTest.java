package io.github.janguenter.allthemons.spawncontroller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import org.junit.jupiter.api.Test;

class PolicyRulesTest {
    private final PolicyRules defaults = PolicyRules.defaults();

    @Test
    void peacefulBlocksEveryNaturalCategory() {
        for (String category : Set.of(
                "monster",
                "creature",
                "ambient",
                "axolotls",
                "underground_water_creature",
                "water_creature",
                "water_ambient"
        )) {
            assertTrue(defaults.blocks(SpawnPolicy.PEACEFUL, category), category);
        }
    }

    @Test
    void easyVoteDefaultsBlockOnlySelectedAquaticPasses() {
        assertTrue(defaults.blocks(SpawnPolicy.EASY_VOTE, "water_creature"));
        assertTrue(defaults.blocks(SpawnPolicy.EASY_VOTE, "water_ambient"));
        assertTrue(defaults.blocks(SpawnPolicy.EASY_VOTE, "axolotls"));

        assertFalse(defaults.blocks(SpawnPolicy.EASY_VOTE, "monster"));
        assertFalse(defaults.blocks(SpawnPolicy.EASY_VOTE, "creature"));
        assertFalse(defaults.blocks(SpawnPolicy.EASY_VOTE, "ambient"));
        assertFalse(defaults.blocks(
                SpawnPolicy.EASY_VOTE,
                "underground_water_creature"
        ));
    }

    @Test
    void easyVoteCreaturePassIsConfigurable() {
        PolicyRules rules = new PolicyRules(Set.of("WATER_CREATURE", "CREATURE"));

        assertTrue(rules.blocks(SpawnPolicy.EASY_VOTE, "creature"));
        assertTrue(rules.blocks(SpawnPolicy.EASY_VOTE, "water_creature"));
        assertFalse(rules.blocks(SpawnPolicy.EASY_VOTE, "monster"));
    }

    @Test
    void stockNeverBlocksAnyCategory() {
        for (String category : Set.of(
                "monster",
                "creature",
                "water_creature",
                "future_mod_category"
        )) {
            assertFalse(defaults.blocks(SpawnPolicy.STOCK, category), category);
        }
    }

    @Test
    void emptyEasyVoteListDisablesEasyVoteBlocking() {
        PolicyRules rules = new PolicyRules(Set.of());

        assertFalse(rules.blocks(SpawnPolicy.EASY_VOTE, "water_creature"));
        assertFalse(rules.blocks(SpawnPolicy.EASY_VOTE, "creature"));
        assertFalse(rules.blocks(SpawnPolicy.EASY_VOTE, "monster"));
    }

    @Test
    void configuredCategoriesAreNormalizedAndImmutable() {
        PolicyRules rules = new PolicyRules(Set.of(" Water_Ambient "));

        assertEquals(Set.of("water_ambient"), rules.easyVoteBlockedCategories());
        assertThrows(
                UnsupportedOperationException.class,
                () -> rules.easyVoteBlockedCategories().add("creature")
        );
    }
}
