package net.titanrealms.api.languageapi.models.server;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

public enum ServerType {

    GLOBAL("global"), // used for lang classification
    LOBBY("lobby"),
    PROXY("proxy"),
    PLAYER_CELL("player-cell");

    @NonNull
    private final String identifier;

    ServerType(@NonNull String identifier) {
        this.identifier = identifier;
    }

    @NonNull
    public String getIdentifier() {
        return this.identifier;
    }

    @Nullable
    public ServerType fromId(String identifier) {
        for (ServerType serverType : ServerType.values())
            if (serverType.getIdentifier().equals(identifier))
                return serverType;
        return null;
    }
}
