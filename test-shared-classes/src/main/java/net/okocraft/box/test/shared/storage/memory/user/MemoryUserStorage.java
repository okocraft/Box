package net.okocraft.box.test.shared.storage.memory.user;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryUserStorage implements UserStorage {

    private final Map<UUID, String> uuidToNameMap = new HashMap<>();
    private final Map<String, UUID> nameToUuidMap = new HashMap<>();

    @Override
    public void init() {
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) {
        return BoxUserFactory.create(uuid, this.uuidToNameMap.get(uuid));
    }

    @Override
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) {
        this.uuidToNameMap.put(uuid, name);

        if (name != null) {
            this.nameToUuidMap.put(name, uuid);
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) {
        var uuid = this.nameToUuidMap.get(name);
        return uuid != null ? BoxUserFactory.create(uuid, name) : null;
    }

    @Override
    public @NotNull Collection<BoxUser> loadAllBoxUsers() {
        var list = new ArrayList<BoxUser>(this.uuidToNameMap.size());

        for (var entry : this.uuidToNameMap.entrySet()) {
            list.add(BoxUserFactory.create(entry.getKey(), entry.getValue()));
        }

        return list;
    }

    @Override
    public void saveBoxUsers(@NotNull Collection<BoxUser> users) {
        for (var user : users) {
            saveBoxUser(user.getUUID(), user.getName().orElse(null));
        }
    }
}
