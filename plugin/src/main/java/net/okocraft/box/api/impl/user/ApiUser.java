package net.okocraft.box.api.impl.user;

import net.okocraft.box.Box;
import net.okocraft.box.api.item.BoxItem;
import net.okocraft.box.api.user.User;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ApiUser implements User {

    private final UUID uuid;
    private final String name;

    public ApiUser(@NotNull UUID uuid, @NotNull String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public @NotNull UUID getUniqueID() {
        return uuid;
    }

    @Override
    public @NotNull String getUserName() {
        return name;
    }

    @Override
    public int getAmount(@NotNull BoxItem item) {
        return Box.getInstance().getAPI().getPlayerData().getStock(getOfflinePlayer(), item.toItemStack());
    }

    @Override
    public void setAmount(@NotNull BoxItem item, int amount) {
        Box.getInstance().getAPI().getPlayerData().setStock(getOfflinePlayer(), item.toItemStack(), amount);
    }

    @Override
    public int increase(@NotNull BoxItem item, int amount) {
        Box.getInstance().getAPI().getPlayerData().addStock(getOfflinePlayer(), item.toItemStack(), amount);
        return getAmount(item);
    }

    @Override
    public int decrease(@NotNull BoxItem item, int amount) {
        Box.getInstance().getAPI().getPlayerData().addStock(getOfflinePlayer(), item.toItemStack(), -1 * amount);
        return getAmount(item);
    }

    @Override
    public boolean isAutoStore(@NotNull BoxItem item) {
        return Box.getInstance().getAPI().getPlayerData().getAutoStore(getOfflinePlayer(), item.toItemStack());
    }

    @Override
    public void setAutoStore(@NotNull BoxItem item, boolean bool) {
        Box.getInstance().getAPI().getPlayerData().setAutoStore(getOfflinePlayer(), item.toItemStack(), bool);
    }

    @NotNull
    private OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }
}
