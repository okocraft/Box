package net.okocraft.box.storage.implementation.yaml;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.locks.StampedLock;

class YamlUserStorage implements UserStorage {

    private final Path filepath;
    private UserMap userMap;

    YamlUserStorage(@NotNull Path rootDirectory) {
        this.filepath = rootDirectory.resolve("users.yml");
    }

    @Override
    public void init() throws Exception {
        if (!Files.isRegularFile(this.filepath)) {
            this.userMap = new UserMap();
            return;
        }

        var map = new Object2ObjectOpenHashMap<UUID, String>();

        try (var reader = Files.newBufferedReader(this.filepath, StandardCharsets.UTF_8);
             var lines = reader.lines()) {
            lines.forEach(line -> {
                var separatorIndex = line.indexOf(": ");

                if (separatorIndex == -1 || line.length() <= separatorIndex + 2) {
                    return;
                }

                var uuid = UUIDParser.parseOrWarn(line.substring(0, separatorIndex));
                var name = line.substring(separatorIndex + 2);

                if (uuid != null) {
                    map.put(uuid, name.equals("\"\"") ? "" : name);
                }
            });
        }

        this.userMap = new UserMap(map);
    }

    @Override
    public @NotNull BoxUser loadBoxUser(@NotNull UUID uuid) {
        return BoxUserFactory.create(uuid, this.userMap.searchForUsername(uuid));
    }

    @Override
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception {
        this.userMap.putUUIDAndUsername(uuid, name != null ? name : "");

        synchronized (this) {
            this.saveUserMap();
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) {
        var uuid = this.userMap.searchForUUID(name);
        return uuid != null ? BoxUserFactory.create(uuid, this.userMap.searchForUsername(uuid)) : null;
    }

    @Override
    public @NotNull Collection<BoxUser> loadAllBoxUsers() {
        return this.userMap.getAllUsers();
    }

    @Override
    public void saveBoxUsers(@NotNull Collection<BoxUser> users) throws Exception {
        for (var user : users) {
            this.userMap.putUUIDAndUsername(user.getUUID(), user.getName().orElse(""));
        }

        synchronized (this) {
            this.saveUserMap();
        }
    }

    private void saveUserMap() throws Exception {
        var snapshot = this.userMap.getSnapshotIfDirty();

        if (snapshot == null) {
            return;
        }

        try (var writer = Files.newBufferedWriter(this.filepath, StandardCharsets.UTF_8, YamlFileOptions.WRITE)) {
            for (var entry : snapshot.object2ObjectEntrySet()) {
                writer.write(entry.getKey().toString());
                writer.write(':');
                writer.write(' ');
                writer.write(entry.getValue().isEmpty() ? "\"\"" : entry.getValue());
                writer.newLine();
            }
        }
    }

    static class UserMap {

        private final Object2ObjectMap<UUID, String> uuidToNameMap;
        private final Object2ObjectMap<NameKey, UUID> nameToUUIDMap;
        private final StampedLock lock = new StampedLock();
        private volatile boolean dirty;

        UserMap() {
            this.uuidToNameMap = new Object2ObjectOpenHashMap<>();
            this.nameToUUIDMap = new Object2ObjectOpenHashMap<>();
        }

        UserMap(@NotNull Object2ObjectMap<UUID, String> uuidToNameMap) {
            this.uuidToNameMap = uuidToNameMap;
            this.nameToUUIDMap = new Object2ObjectOpenHashMap<>(Math.max(Object2ObjectOpenHashMap.DEFAULT_INITIAL_SIZE, uuidToNameMap.size()));

            this.uuidToNameMap.object2ObjectEntrySet().forEach(entry -> {
                var name = entry.getValue();

                if (!name.isEmpty()) {
                    this.nameToUUIDMap.put(new NameKey(entry.getValue()), entry.getKey());
                }
            });
        }

        void putUUIDAndUsername(@NotNull UUID uuid, @NotNull String name) {
            long stamp = this.lock.writeLock();

            try {
                var oldName = this.uuidToNameMap.put(uuid, name);

                if (oldName != null) {
                    this.nameToUUIDMap.remove(new NameKey(oldName));
                }

                if (!name.isEmpty()) {
                    this.nameToUUIDMap.put(new NameKey(name), uuid);
                }
                this.dirty = true;
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }

        @Nullable String searchForUsername(@NotNull UUID uuid) {
            {
                long stamp = this.lock.tryOptimisticRead();
                var result = this.uuidToNameMap.get(uuid);

                if (this.lock.validate(stamp)) {
                    return result;
                }
            }

            long stamp = this.lock.readLock();

            try {
                return this.uuidToNameMap.get(uuid);
            } finally {
                this.lock.unlockRead(stamp);
            }
        }

        @Nullable UUID searchForUUID(@NotNull String name) {
            var key = new NameKey(name);

            {
                long stamp = this.lock.tryOptimisticRead();
                var result = this.nameToUUIDMap.get(key);

                if (this.lock.validate(stamp)) {
                    return result;
                }
            }

            long stamp = this.lock.readLock();

            try {
                return this.nameToUUIDMap.get(key);
            } finally {
                this.lock.unlockRead(stamp);
            }
        }

        @NotNull Collection<BoxUser> getAllUsers() {
            long stamp = this.lock.readLock();

            try {
                var result = new ArrayList<BoxUser>(this.uuidToNameMap.size());

                for (var entry : this.uuidToNameMap.object2ObjectEntrySet()) {
                    var name = entry.getValue();
                    result.add(BoxUserFactory.create(entry.getKey(), name.isEmpty() ? null : name));
                }

                return result;
            } finally {
                this.lock.unlockRead(stamp);
            }
        }

        @Nullable Object2ObjectMap<UUID, String> getSnapshotIfDirty() {
            {
                long stamp = this.lock.tryOptimisticRead();
                boolean dirty = this.dirty;

                if (this.lock.validate(stamp) && !dirty) {
                    return null;
                }
            }

            long stamp = this.lock.writeLock();

            try {
                if (!this.dirty) {
                    return null;
                }

                this.dirty = false;
                var snapshot = new Object2ObjectOpenHashMap<UUID, String>(this.uuidToNameMap.size());
                this.uuidToNameMap.object2ObjectEntrySet().forEach(entry -> snapshot.put(entry.getKey(), entry.getValue()));
                return snapshot;
            } finally {
                this.lock.unlockWrite(stamp);
            }
        }

        private static final class NameKey {

            private final String name;
            private final String lowercase;

            private NameKey(@NotNull String name) {
                this.name = name;
                this.lowercase = name.toLowerCase(Locale.ENGLISH);
            }

            @Override
            public boolean equals(Object object) {
                if (this == object) return true;
                if (object == null || this.getClass() != object.getClass()) return false;
                NameKey other = (NameKey) object;
                return this.lowercase.equals(other.lowercase);
            }

            @Override
            public int hashCode() {
                return this.lowercase.hashCode();
            }

            @Override
            public String toString() {
                return "NameKey{" +
                        "name='" + this.name + '\'' +
                        ", lowercase='" + this.lowercase + '\'' +
                        '}';
            }
        }
    }
}
