package net.okocraft.box.storage.api.factory.user;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

class BoxUserImpl implements BoxUser {

    private final UUID uuid;

    private String name;

    BoxUserImpl(@NotNull UUID uuid, @Nullable String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @NotNull UUID getUUID() {
        return this.uuid;
    }

    @Override
    public @NotNull Optional<String> getName() {
        return Optional.ofNullable(this.name);
    }

    public void setName(@NotNull String name) {
        this.name = Objects.requireNonNull(name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        return o instanceof BoxUserImpl boxUser && this.uuid.equals(boxUser.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.uuid);
    }

    @Override
    public String toString() {
        return "BoxUserImpl{" +
            "uuid=" + this.uuid +
            ", name='" + this.name + '\'' +
            '}';
    }
}

