package net.okocraft.box.storage.api.factory.user;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class BoxUserFactory {

    @Contract("_ -> new")
    public static @NotNull BoxUser create(@NotNull UUID uuid) {
        return create(uuid, null);
    }

    @Contract("_, _ -> new")
    public static @NotNull BoxUser create(@NotNull UUID uuid, @Nullable String username) {
        return new BoxUserImpl(uuid, username == null || username.isEmpty() ? null : username);
    }

    private BoxUserFactory() {
        throw new UnsupportedOperationException();
    }
}
