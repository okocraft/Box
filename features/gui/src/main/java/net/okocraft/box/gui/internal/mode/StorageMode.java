package net.okocraft.box.gui.internal.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.gui.api.util.TransactionAmountHolder;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StorageMode implements BoxItemClickMode {

    @Override
    public @NotNull String getName() {
        return "storage";
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        if (context.clickType().isLeftClick()) {
            processDeposit(context);
            return;
        }

        if (context.clickType().isRightClick()) {
            processWithdraw(context);
        }
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var result = new ArrayList<Component>();

        var original = target.lore();

        if (original != null) {
            result.addAll(original);
        }

        result.add(Component.empty());
        result.addAll(TranslationUtil.render(createLore(item, viewer), viewer));

        target.lore(result);
    }

    private @NotNull @Unmodifiable List<Component> createLore(@NotNull BoxItem item, @NotNull Player player) {
        int currentStock = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder().getAmount(item);
        int transactionAmount = TransactionAmountHolder.getAmount(player);

        return List.of(
                Displays.STORAGE_MODE_LEFT_CLICK_TO_DEPOSIT.apply(transactionAmount),
                Displays.STORAGE_MODE_RIGHT_CLICK_TO_WITHDRAW.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }

    private void processDeposit(@NotNull Context context) {
        var player = context.clicker();

        var resultList = CompletableFuture.supplyAsync(
                () -> InventoryTransaction.depositItem(
                        player.getInventory(),
                        context.item(),
                        TransactionAmountHolder.getAmount(player)
                ),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();

        if (!resultList.getType().isModified()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            return;
        }

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();

        resultList.getResultList()
                .stream()
                .filter(result -> result.getType().isModified())
                .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount()));

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 100f, 1.0f);
    }

    private void processWithdraw(@NotNull Context context) {
        var player = context.clicker();

        var stockHolder = BoxProvider.get().getBoxPlayerMap().get(player).getCurrentStockHolder();
        var currentStock = stockHolder.getAmount(context.item());

        if (currentStock < 1) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            return;
        }

        var amount = Math.min(currentStock, TransactionAmountHolder.getAmount(player));

        var result = CompletableFuture.supplyAsync(
                () -> InventoryTransaction.withdraw(player.getInventory(), context.item(), amount),
                BoxProvider.get().getExecutorProvider().getMainThread()
        ).join();

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount());
            player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_ON, 100f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
        }
    }
}
