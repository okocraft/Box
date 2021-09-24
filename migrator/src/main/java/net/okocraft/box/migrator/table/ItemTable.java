package net.okocraft.box.migrator.table;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxCustomItem;
import net.okocraft.box.migrator.database.Database;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/*
 * source:
 * https://github.com/okocraft/Box/blob/master/src/main/java/net/okocraft/box/database/ItemTable.java
 */
public class ItemTable {

    private final Database database;

    public ItemTable(@NotNull Database database) {
        this.database = database;
    }

    public @NotNull Map<Integer, ItemStack> loadAndMigrate() {
        var itemManager = BoxProvider.get().getItemManager();
        var itemMap = new HashMap<Integer, ItemStack>(500);

        database.execute(
                "SELECT id, item, customname FROM box_items",
                rs -> {
                    while (rs.next()) {
                        var base64Data = rs.getString("item");

                        if (base64Data == null || base64Data.isEmpty()) {
                            return;
                        }

                        var item = fromString(base64Data);

                        if (item == null || item.getType().isAir() || !item.getType().isItem()) {
                            return;
                        }

                        item.setAmount(1);

                        itemMap.put(rs.getInt("id"), item);

                        var boxItem = itemManager.getBoxItem(item);

                        // migrate custom item and its name
                        BoxCustomItem customItem = null;
                        if (boxItem.isEmpty()) {
                            customItem = itemManager.registerCustomItem(item).join();
                        } else {
                            if (itemManager.isCustomItem(boxItem.get())) {
                                customItem = (BoxCustomItem) boxItem.get();
                            }
                        }

                        if (customItem != null) {
                            var customName = rs.getString("customname");

                            if (customName != null) {
                                itemManager.renameCustomItem(customItem, customName).join();
                            }
                        }
                    }
                });

        return itemMap;
    }

    private @Nullable ItemStack fromString(String base64) {
        try {
            String data = new String(Base64.getDecoder().decode(base64));
            YamlConfiguration yaml = new YamlConfiguration();
            yaml.loadFromString(data);
            return yaml.getItemStack("i");
        } catch (InvalidConfigurationException | IllegalArgumentException e) {
            e.printStackTrace();
            return null;
        }
    }
}
