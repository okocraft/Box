package net.okocraft.box.plugin.api.item;

import net.okocraft.box.Box;
import net.okocraft.box.api.item.BoxItem;
import net.okocraft.box.api.item.ItemManager;
import net.okocraft.box.database.ItemData;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

public class ApiItemManager implements ItemManager {

    @Override
    public @Nullable BoxItem searchItem(@NotNull String name) {
        ItemStack item = Box.getInstance().getAPI().getItemData().getItemStack(name);
        return item != null ? new ApiBoxItem(item) : null;
    }

    @Override
    public @NotNull @Unmodifiable Collection<BoxItem> getAllItems() {
        ItemData itemData = Box.getInstance().getAPI().getItemData();
        return itemData.getNames().stream()
                .map(itemData::getItemStack)
                .filter(Objects::nonNull)
                .map(ApiBoxItem::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    public @Nullable BoxItem register(@NotNull ItemStack item) {
        String name = Box.getInstance().getAPI().getItemData().register(item);
        return name.isEmpty() ? null : new ApiBoxItem(item);
    }

    @Override
    public boolean isRegistered(@NotNull ItemStack item) {
        return Box.getInstance().getAPI().getItemData().getName(item) != null;
    }

    @Override
    public boolean isRegistered(@NotNull String name) {
        return Box.getInstance().getAPI().getItemData().getItemStack(name) != null;
    }
}
