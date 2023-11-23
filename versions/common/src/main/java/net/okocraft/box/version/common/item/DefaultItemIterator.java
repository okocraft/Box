package net.okocraft.box.version.common.item;

import net.okocraft.box.storage.api.util.item.DefaultItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

abstract class DefaultItemIterator implements Iterator<DefaultItem> {
    protected <IM> @NotNull IM createItemMeta(@NotNull Material material, @NotNull Class<IM> clazz) {
        var meta = Bukkit.getItemFactory().getItemMeta(material);
        if (clazz.isInstance(meta)) {
            return clazz.cast(meta);
        } else {
            throw new IllegalStateException("Where has " + clazz.getSimpleName() + " gone!?");
        }
    }
}
