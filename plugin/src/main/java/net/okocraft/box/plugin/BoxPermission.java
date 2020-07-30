package net.okocraft.box.plugin;

import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;

public enum BoxPermission {

    BOX_AUTO_STORE("box.autostore", true),

    BOX_STICK_BREAK("box.stick.break", true),
    BOX_STICK_CONSUME("box.stick.consume", true),
    BOX_STICK_OPEN("box.stick.open", true),
    BOX_STICK_PLACE("box.stick.place", true),
    BOX_STICK_THROW("box.stick.throw", true);

    private final static Map<BoxPermission, Permission> PERMISSIONS;

    static {
        Map<BoxPermission, Permission> result = new HashMap<>();

        for (BoxPermission perm : values()) {
            PermissionDefault def = perm.isDefault() ? PermissionDefault.TRUE : PermissionDefault.OP;
            result.put(perm, new Permission(perm.getNode(), def));
        }

        PERMISSIONS = Map.copyOf(result);
    }

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
        return permissible.hasPermission(PERMISSIONS.get(this));
    }

    @NotNull
    @Unmodifiable
    public static Map<BoxPermission, Permission> getPermissions() {
        return PERMISSIONS;
    }
}
