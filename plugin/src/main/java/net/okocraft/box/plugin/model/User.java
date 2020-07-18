package net.okocraft.box.plugin.model;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class User extends BoxDataHolder {

    private final UUID uuid;
    private final String name;

    public User(@NotNull UUID uuid, @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
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
