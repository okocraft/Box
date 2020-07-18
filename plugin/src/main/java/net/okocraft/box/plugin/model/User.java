package net.okocraft.box.plugin.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class User extends BoxDataHolder {

    private final int internalID;
    private final UUID uuid;
    private final String name;

    public User(int internalID, @NotNull UUID uuid, @NotNull String name) {
        this.internalID = internalID;
        this.uuid = uuid;
        this.name = name;
    }

    public int getInternalID() {
        return internalID;
    }

    @NotNull
    public UUID getUuid() {
        return uuid;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
