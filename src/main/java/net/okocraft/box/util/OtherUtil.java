package net.okocraft.box.util;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;

public class OtherUtil {

    /**
     * numberを解析してint型にして返す。numberのフォーマットがintではないときはdefを返す。
     * 
     * @author LazyGon
     * @since v1.1.0
     * 
     * @param number
     * @param def
     * 
     * @return int型の数字。
     */
    public static int parseIntOrDefault(@Nonnull String number, int def) {
        try {
            return Integer.parseInt(number);
        } catch (NumberFormatException exception) {
            return def;
        }
    }

    /**
     * numberを解析してlong型にして返す。numberのフォーマットがlongではないときはdefを返す。
     * 
     * @author LazyGon
     * @since v1.1.0
     * 
     * @param number
     * @param def
     * 
     * @return long型の数字。
     */
    public static long parseLongOrDefault(@Nonnull String number, long def) {
        try {
            return Long.parseLong(number);
        } catch (NumberFormatException exception) {
            return def;
        }
    }

    /**
     * パーミッションを登録する。
     * 
     * @param permName 登録する権限
     * @param parentPermName 登録する権限の親
     */
    public static void registerPermission(String permName, @Nullable String parentPermName) {
        PluginManager pm = Bukkit.getPluginManager();
        Optional<Permission> optionalPerm = Optional.ofNullable(pm.getPermission(permName));

        if (!optionalPerm.isPresent()) {
            Permission perm = new Permission(permName);

            Optional.ofNullable(parentPermName).ifPresent(_parentPermName -> {
                Optional.ofNullable(pm.getPermission(_parentPermName)).ifPresent(parentPerm -> {
                    perm.addParent(parentPerm, true);
                });
            });

            pm.getPermissions().add(perm);
        }
    }

    /**
     * パーミッションを登録する。
     * 
     * @param permName
     */
    public static void registerPermission(String permName) {
        registerPermission(permName, null);
    }
}