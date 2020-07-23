package net.okocraft.box.plugin.model.manager;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.result.RegistrationResult;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.logging.Level;

public class ItemManager {

    private final Box plugin;

    public ItemManager(@NotNull Box plugin) {
        this.plugin = plugin;
    }

    @NotNull
    public Optional<Item> getItem(@NotNull ItemStack item) {
        return plugin.getStorage().getItems().stream().filter(i -> i.getOriginal().isSimilar(item)).findFirst();
    }

    @NotNull
    public Optional<Item> getItemById(int id) {
        return plugin.getStorage().getItems().stream().filter(i -> i.getInternalID() == id).findFirst();
    }

    @NotNull
    public RegistrationResult registerItem(@NotNull ItemStack item) {
        Optional<Item> boxItem = getItem(item);
        if (boxItem.isPresent()) {
            return RegistrationResult.ALREADY_REGISTERED;
        }

        item.setAmount(1);

        try {
            plugin.getStorage().registerItem(item).join();
            return RegistrationResult.SUCCESS;
        } catch (Throwable e) {
            plugin.getLogger().log(Level.SEVERE, "Could not register item.", e);
            return RegistrationResult.EXCEPTION_OCCURS;
        }
    }

    public void setCustomName(@NotNull Item item, @NotNull String newName) {
        item.setCustomName(newName);
        plugin.getStorage().saveCustomName(item);
    }
}
