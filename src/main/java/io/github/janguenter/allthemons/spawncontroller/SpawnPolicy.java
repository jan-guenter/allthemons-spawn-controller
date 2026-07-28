package io.github.janguenter.allthemons.spawncontroller;

import java.util.Arrays;
import java.util.Optional;

/**
 * Explicit operating states understood by the server command and KubeJS.
 */
public enum SpawnPolicy {
    PEACEFUL("peaceful"),
    EASY_VOTE("easy_vote"),
    STOCK("stock");

    private final String commandName;

    SpawnPolicy(String commandName) {
        this.commandName = commandName;
    }

    public String commandName() {
        return commandName;
    }

    public static Optional<SpawnPolicy> fromCommandName(String value) {
        return Arrays.stream(values())
                .filter(policy -> policy.commandName.equals(value))
                .findFirst();
    }
}
