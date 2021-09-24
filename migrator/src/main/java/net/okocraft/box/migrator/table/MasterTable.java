package net.okocraft.box.migrator.table;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.feature.autostore.model.AutoStoreSetting;
import net.okocraft.box.feature.autostore.model.SettingManager;
import net.okocraft.box.migrator.database.Database;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Optional;

/*
 * source:
 * https://github.com/okocraft/Box/blob/master/src/main/java/net/okocraft/box/database/MasterTable.java
 */
public class MasterTable {

    private final Database database;
    private final Map<Integer, ItemStack> itemIdMap;

    public MasterTable(@NotNull Database database, @NotNull Map<Integer, ItemStack> itemIdMap) {
        this.database = database;
        this.itemIdMap = itemIdMap;
    }

    public void migrate(@NotNull BoxUser user, int id) {
        var player = Bukkit.getPlayer(user.getUUID());
        UserStockHolder stockHolder;
        AutoStoreSetting autoStoreSetting;

        if (player != null) {
            stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getUserStockHolder();
        } else {
            stockHolder = BoxProvider.get().getStockManager().loadUserStock(user).join();
        }

        database.execute(
                "SELECT itemid, stock, autostore FROM box_master WHERE playerid = '" + id + "'",
                rs -> {
                    while (rs.next()) {
                        int itemId = rs.getInt("itemid");
                        int stock = rs.getInt("stock");

                        if (stock < 1) {
                            continue;
                        }

                        Optional.ofNullable(itemIdMap.get(itemId))
                                .flatMap(BoxProvider.get().getItemManager()::getBoxItem)
                                .ifPresentOrElse(
                                        boxItem -> stockHolder.increase(boxItem, stock),
                                        () -> BoxProvider.get().getLogger().warning(
                                                "Could not found item (" + "itemid=" + itemId +
                                                        ", playerId=" + id + ", stock=" + stock + ")"
                                        )
                                );
                    }
                });

        if (!stockHolder.getStockedItems().isEmpty()) {
            BoxProvider.get().getStockManager().saveUserStock(stockHolder).join();
        }
    }

    private class FakeAutoStoreSetting extends AutoStoreSetting {

        @SuppressWarnings("ConstantConditions")
        public FakeAutoStoreSetting() {
            super(null);
        }
    }
}
