package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

class YamlUserStorage implements UserStorage {

    private final YamlConfiguration userData;

    YamlUserStorage(@NotNull Path rootDirectory) {
        this.userData = YamlConfiguration.create(rootDirectory.resolve("users.yml"));
    }

    @Override
    public void init() throws Exception {
        userData.load();
    }

    @Override
    public @NotNull BoxUser getUser(@NotNull UUID uuid) {
        var name = userData.getString(uuid.toString());
        return BoxUserFactory.create(uuid, name.isEmpty() ? null : name);
    }

    @Override
    public void saveBoxUser(@NotNull BoxUser user) throws Exception {
        if (user.getName().isPresent()) {
            userData.set(user.getUUID().toString(), user.getName().get());
            userData.save();
        }
    }

    @Override
    public void saveBoxUserIfNotExists(@NotNull BoxUser user) throws Exception {
        if (user.getName().isPresent() && userData.get(user.getUUID().toString()) == null) {
            userData.set(user.getUUID().toString(), user.getName().get());
            userData.save();
        }
    }

    @Override
    public @NotNull Optional<BoxUser> search(@NotNull String name) throws Exception {
        String strUuid = null;

        for (var key : userData.getKeyList()) {
            if (userData.getString(key).equalsIgnoreCase(name)) {
                strUuid = key;
            }
        }

        if (strUuid == null) {
            return Optional.empty();
        }

        var uuid = UUIDParser.parseOrWarn(strUuid);

        if (uuid == null) {
            userData.set(strUuid, null); // remove invalid uuid
            userData.save();
            return Optional.empty();
        }

        var savedName = userData.getString(strUuid);

        return Optional.of(BoxUserFactory.create(uuid, savedName));
    }
}
