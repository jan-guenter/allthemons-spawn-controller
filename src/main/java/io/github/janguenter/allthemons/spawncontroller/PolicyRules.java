package io.github.janguenter.allthemons.spawncontroller;

import java.util.Collection;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Pure policy logic. Category names are Minecraft serialized names, not Java enum constants.
 */
public final class PolicyRules {
    public static final Set<String> DEFAULT_EASY_VOTE_BLOCKED_CATEGORIES = Set.of(
            "water_creature",
            "water_ambient",
            "axolotls"
    );

    private final Set<String> easyVoteBlockedCategories;

    public PolicyRules(Collection<? extends String> easyVoteBlockedCategories) {
        this.easyVoteBlockedCategories = easyVoteBlockedCategories.stream()
                .map(PolicyRules::normalize)
                .collect(Collectors.toUnmodifiableSet());
    }

    public static PolicyRules defaults() {
        return new PolicyRules(DEFAULT_EASY_VOTE_BLOCKED_CATEGORIES);
    }

    public boolean blocks(SpawnPolicy policy, String serializedCategoryName) {
        return switch (policy) {
            case PEACEFUL -> true;
            case EASY_VOTE -> easyVoteBlockedCategories.contains(normalize(serializedCategoryName));
            case STOCK -> false;
        };
    }

    public Set<String> easyVoteBlockedCategories() {
        return easyVoteBlockedCategories;
    }

    private static String normalize(String categoryName) {
        return categoryName.strip().toLowerCase(Locale.ROOT);
    }
}
