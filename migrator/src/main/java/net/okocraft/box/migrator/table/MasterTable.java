package net.okocraft.box.migrator.table;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.migrator.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/*
 * source:
 * https://github.com/okocraft/Box/blob/v3/master/src/main/java/net/okocraft/box/database/MasterTable.java
 */
@SuppressWarnings("ClassCanBeRecord")
public class MasterTable {

    private final Database database;
    private final Map<Integer, ItemStack> itemIdMap;

    public MasterTable(@NotNull Database database, @NotNull Map<Integer, ItemStack> itemIdMap) {
        this.database = database;
        this.itemIdMap = itemIdMap;
    }

    public void migrate(@NotNull BoxUser user, int id) {
        BoxProvider.get().getLogger()
                .info("Migrating player: " + user.getUUID() + " (" + user.getName().orElse("UNKNOWN") + ")");

        var player = Bukkit.getPlayer(user.getUUID());
        UserStockHolder stockHolder;
        AutoStoreSetting autoStoreSetting;

        if (player != null) {
            stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getUserStockHolder();
            autoStoreSetting = AutoStoreSettingContainer.INSTANCE.get(player);
        } else {
            stockHolder = BoxProvider.get().getStockManager().loadUserStock(user).join();
            autoStoreSetting = new AutoStoreSetting(user.getUUID());
        }

        var autoStoreMigrator = new AutoStoreSettingMigrator(autoStoreSetting);

        database.execute(
                "SELECT itemid, stock, autostore FROM box_master WHERE playerid = '" + id + "'",
                rs -> {
                    while (rs.next()) {
                        int itemId = rs.getInt("itemid");
                        int stock = rs.getInt("stock");
                        boolean autostore = rs.getInt("autostore") == 1;

                        var itemStack = itemIdMap.get(itemId);

                        if (itemStack == null || itemStack.getType().isAir()) {
                            continue;
                        }

                        var item = BoxProvider.get().getItemManager().getBoxItem(itemStack);

                        if (item.isEmpty()) {
                            BoxProvider.get().getLogger().warning(
                                    "Could not found item (" +
                                            "itemid=" + itemId +
                                            ", playerId=" + id +
                                            ", stock=" + stock +
                                            ", autostore=" + autostore +
                                            ")"
                            );
                            continue;
                        }

                        var boxItem = item.get();

                        if (0 < stock) {
                            stockHolder.increase(boxItem, stock);
                        }

                        autoStoreMigrator.migrate(boxItem, autostore);
                    }
                });

        if (!stockHolder.getStockedItems().isEmpty()) {
            BoxProvider.get().getStockManager().saveUserStock(stockHolder).join();
        }

        autoStoreMigrator.finish();

        if (autoStoreMigrator.shouldSave()) {
            AutoStoreSettingContainer.INSTANCE.save(autoStoreSetting).join();
        }
    }

    private static class AutoStoreSettingMigrator {

        private final AutoStoreSetting target;
        private Boolean previousSetting = null;
        private boolean perItemFlag = false;

        private final List<BoxItem> processedItems = new ArrayList<>();

        private AutoStoreSettingMigrator(@NotNull AutoStoreSetting target) {
            this.target = target;
        }

        private void migrate(@NotNull BoxItem item, boolean bool) {
            if (perItemFlag) {
                target.getPerItemModeSetting().setEnabled(item, bool);
                return;
            }

            if (previousSetting == null) {
                previousSetting = bool;
                processedItems.add(item);
                return;
            }

            if (previousSetting == bool) {
                processedItems.add(item);
                return;
            }

            perItemFlag = true;

            for (var processed : processedItems) {
                target.getPerItemModeSetting().setEnabled(processed, previousSetting);
            }

            processedItems.clear();
            target.getPerItemModeSetting().setEnabled(item, bool);
        }

        private void finish() {
            target.setAllMode(!perItemFlag);
        }

        private boolean shouldSave() {
            return perItemFlag;
        }
    }
}
