package net.okocraft.box.feature.begui.internal.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.begui.internal.lang.Displays;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.AdditionalButton;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

public class StorageDepositMode implements BoxItemClickMode {

    private static final String TRANSACTION_AMOUNT_NAME = "transaction-amount";

    @Override
    public @NotNull String getName() {
        return "storage_deposit";
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_MODE_DEPOSIT_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        processDeposit(context);
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var newLore = Optional.ofNullable(target.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        newLore.add(Component.empty());
        newLore.addAll(TranslationUtil.render(createLore(item, viewer), viewer));

        target.lore(newLore);
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public @NotNull AdditionalButton createAdditionalButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        return new DepositAllButton();
    }

    @Override
    public Set<GuiType> getApplicableGuiTypes() {
        return Set.of(GuiType.BE);
    }

    private @NotNull @Unmodifiable List<Component> createLore(@NotNull BoxItem item, @NotNull Player player) {
        var session = PlayerSession.get(player);

        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        return List.of(
                Displays.STORAGE_MODE_DEPOSIT_CLICK_TO_DEPOSIT.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }

    private void processDeposit(@NotNull Context context) {
        var player = context.clicker();
        var session = PlayerSession.get(player);

        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        var resultList =
                BoxProvider.get().getTaskFactory().supply(
                        () -> InventoryTransaction.depositItem(player.getInventory(), context.item(), transactionAmount)
                ).join();

        if (!resultList.getType().isModified()) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
            return;
        }

        var stockHolder = session.getStockHolder();

        resultList.getResultList()
                .stream()
                .filter(result -> result.getType().isModified())
                .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount()));

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 100f, 1.0f);
    }

    private static class DepositAllButton extends AdditionalButton {

        @Override
        public @NotNull Material getIconMaterial() {
            return Material.NETHER_STAR;
        }

        @Override
        public int getIconAmount() {
            return 1;
        }

        @Override
        public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
            target.displayName(TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_DISPLAY_NAME, viewer));

            target.lore(List.of(
                    Component.empty(),
                    TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_1, viewer),
                    TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_2, viewer),
                    Component.empty()
            ));

            return target;
        }

        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            if (!clickType.isShiftClick()) {
                return;
            }

            var resultList =
                    BoxProvider.get().getTaskFactory()
                            .supply(() -> InventoryTransaction.depositItemsInInventory(clicker.getInventory()))
                            .join();

            if (!resultList.getType().isModified()) {
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 100f, 1.5f);
                return;
            }

            var stockHolder = PlayerSession.get(clicker).getStockHolder();

            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount()));

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100f, 2.0f);
        }
    }
}
