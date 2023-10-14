package net.okocraft.box.storage.implementation.yaml;

import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;
import net.okocraft.box.storage.api.model.user.UserStorage;
import net.okocraft.box.storage.api.util.uuid.UUIDParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
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
    public void saveBoxUser(@NotNull UUID uuid, @Nullable String name) throws Exception {
        var strUUID = uuid.toString();

        if (name != null || (this.userData.get(strUUID) == null)) {
            this.userData.set(strUUID, name != null ? name : "");
            this.userData.save();
        }
    }

    @Override
    public @Nullable BoxUser searchByName(@NotNull String name) throws Exception {
        String strUuid = null;

        for (var key : userData.getKeyList()) {
            if (userData.getString(key).equalsIgnoreCase(name)) {
                strUuid = key;
            }
        }

        if (strUuid == null) {
            return null;
        }

        var uuid = UUIDParser.parseOrWarn(strUuid);

        if (uuid == null) {
            userData.set(strUuid, null); // remove invalid uuid
            userData.save();
            return null;
        }

        var savedName = userData.getString(strUuid);

        return BoxUserFactory.create(uuid, savedName);
    }

    @Override
    public @NotNull Collection<BoxUser> getAllUsers() {
        var result = new ArrayList<BoxUser>();

        for (var key : userData.getKeyList()) {
            var uuid = UUIDParser.parseOrWarn(key);

            if (uuid != null) {
                var name = userData.getString(key);
                result.add(BoxUserFactory.create(uuid, name.isEmpty() ? null : name));
            }
        }

        return result;
    }
}
