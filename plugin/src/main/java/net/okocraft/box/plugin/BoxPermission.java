package net.okocraft.box.plugin;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum BoxPermission {

    BOX_AUTO_STORE("box.autostore", true),

    BOX_STICK_BREAK("box.stick.break", true),
    BOX_STICK_CONSUME("box.stick.consume", true),
    BOX_STICK_OPEN("box.stick.open", true),
    BOX_STICK_PLACE("box.stick.place", true),
    BOX_STICK_THROW("box.stick.throw", true);
    private final static LoadingCache<BoxPermission, Permission> CACHE =
            CacheBuilder.newBuilder().expireAfterAccess(5, TimeUnit.MINUTES).build(CacheLoader.from(BoxPermission::getPermission));

    private final String node;
    private final boolean def;

    BoxPermission(@NotNull String node, boolean def) {
        this.node = node;
        this.def = def;
    }

    @NotNull
    public String getNode() {
        return node;
    }

    public boolean isDefault() {
        return def;
    }

    public boolean has(@NotNull Permissible permissible) {
        try {
            return permissible.hasPermission(CACHE.get(this));
        } catch (ExecutionException e) {
            return permissible.hasPermission(getNode());
        }
    }

    @Contract(" -> new")
    @NotNull
    public Permission getPermission() {
        PermissionDefault def = isDefault() ? PermissionDefault.TRUE : PermissionDefault.OP;
        return new Permission(getNode(), def);
    }
}
