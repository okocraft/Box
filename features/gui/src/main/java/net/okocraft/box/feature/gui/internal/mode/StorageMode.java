package net.okocraft.box.feature.gui.internal.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class StorageMode extends AbstractStorageMode {

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_MODE_DISPLAY_NAME;
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var icon = item.getClonedItem();

        var newLore = Optional.ofNullable(icon.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        newLore.add(Component.empty());
        newLore.addAll(TranslationUtil.render(createLore(session, item), session.getViewer()));

        icon.lore(newLore);

        return icon;
    }

    @Override
    public boolean canUse(@NotNull Player viewer, @NotNull BoxPlayer source) {
        return true; // This is the default mode, so it must always be available to the player.
    }

    @Override
    public void onSelect(@NotNull PlayerSession session) {
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        if (clickType.isLeftClick()) {
            return processDeposit(session, item);
        } else if (clickType.isRightClick()) {
            return processWithdraw(session, item);
        }

        return ClickResult.NO_UPDATE_NEEDED;
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new DepositAllButton(slot);
    }

    private @NotNull @Unmodifiable List<Component> createLore(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = amountData != null ? amountData.getValue() : 1;

        return List.of(
                Displays.STORAGE_MODE_LEFT_CLICK_TO_DEPOSIT.apply(transactionAmount),
                Displays.STORAGE_MODE_RIGHT_CLICK_TO_WITHDRAW.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }

    private static class DepositAllButton extends AbstractDepositAllButton {

        protected DepositAllButton(int slot) {
            super(
                    slot,
                    (session, clickType) -> clickType.isShiftClick(),
                    ClickResult.NO_UPDATE_NEEDED
            );
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            var icon = new ItemStack(Material.NETHER_STAR);
            var viewer = session.getViewer();

            icon.editMeta(target -> {
                target.displayName(TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_DISPLAY_NAME, viewer));

                target.lore(List.of(
                        Component.empty(),
                        TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_1, viewer),
                        TranslationUtil.render(Displays.STORAGE_MODE_DEPOSIT_ALL_BUTTON_LORE_2, viewer),
                        Component.empty()
                ));
            });

            return icon;
        }
    }
}
