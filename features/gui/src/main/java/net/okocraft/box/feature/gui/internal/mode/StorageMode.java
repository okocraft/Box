package net.okocraft.box.feature.gui.internal.mode;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class StorageMode extends AbstractStorageMode {

    private final MessageKey displayName;
    private final MessageKey.Arg1<Integer> leftClickToDeposit;
    private final MessageKey.Arg1<Integer> rightClickToWithdraw;
    private final MessageKey.Arg1<Integer> currentStock;

    private final MessageKey depositAllDisplayName;
    private final MessageKey depositAllLore;

    public StorageMode() {
        this.displayName = MessageKey.key(DisplayKeys.STORAGE_MODE_DISPLAY_NAME);
        this.leftClickToDeposit = MessageKey.arg1(DisplayKeys.STORAGE_MODE_DEPOSIT, Placeholders.AMOUNT);
        this.rightClickToWithdraw = MessageKey.arg1(DisplayKeys.STORAGE_MODE_WITHDRAW, Placeholders.AMOUNT);
        this.currentStock = MessageKey.arg1(DisplayKeys.STORAGE_MODE_CURRENT_STOCK, Placeholders.CURRENT);
        this.depositAllDisplayName = MessageKey.key(DisplayKeys.STORAGE_MODE_DEPOSIT_ALL_DISPLAY_NAME);
        this.depositAllLore = MessageKey.key(DisplayKeys.STORAGE_MODE_DEPOSIT_ALL_LORE);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull PlayerSession session) {
        return this.displayName.asComponent();
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int currentStock = session.getSourceStockHolder().getAmount(item);
        int transactionAmount = amountData != null ? amountData.getValue() : 1;

        return ItemEditor.create()
            .copyLoreFrom(item.getOriginal())
            .loreEmptyLine()
            .loreLine(this.leftClickToDeposit.apply(transactionAmount))
            .loreLine(this.rightClickToWithdraw.apply(transactionAmount))
            .loreEmptyLine()
            .loreLine(this.currentStock.apply(currentStock))
            .applyTo(session.getViewer(), item.getClonedItem());
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        return true; // This is the default mode, so it must always be available to the player.
    }

    @Override
    public @NotNull ClickResult onSelect(@NotNull PlayerSession session) {
        return ClickResult.UPDATE_ICONS;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        if (clickType.isLeftClick()) {
            return this.processDeposit(session, item);
        } else if (clickType.isRightClick()) {
            return this.processWithdraw(session, item);
        }

        return ClickResult.NO_UPDATE_NEEDED;
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new DepositAllButton(slot, this.depositAllDisplayName, this.depositAllLore);
    }

    private static class DepositAllButton extends AbstractDepositAllButton {

        private final MessageKey displayName;
        private final MessageKey lore;

        private DepositAllButton(int slot, @NotNull MessageKey displayName, @NotNull MessageKey lore) {
            super(
                slot,
                (_, clickType) -> clickType.isShiftClick(),
                ClickResult.NO_UPDATE_NEEDED
            );
            this.displayName = displayName;
            this.lore = lore;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            return ItemEditor.create()
                .displayName(this.displayName)
                .loreLines(this.lore)
                .createItem(session.getViewer(), Material.NETHER_STAR);
        }
    }
}
