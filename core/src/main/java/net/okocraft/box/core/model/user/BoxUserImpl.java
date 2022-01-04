package net.okocraft.box.core.model.user;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class BoxUserImpl implements BoxUser {

    private final UUID uuid;

    private String name;

    public BoxUserImpl(@NotNull UUID uuid) {
        this.uuid = uuid;
        this.name = null;
    }

    public BoxUserImpl(@NotNull UUID uuid, @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @NotNull UUID getUUID() {
        return uuid;
    }

    @Override
    public @NotNull Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public void setName(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof BoxUserImpl boxUser && uuid.equals(boxUser.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

    @Override
    public String toString() {
        return "BoxUserImpl{" +
                "uuid=" + uuid +
                ", name='" + name + '\'' +
                '}';
    }
}
