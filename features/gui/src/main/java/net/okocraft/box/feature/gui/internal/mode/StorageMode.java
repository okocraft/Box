package net.okocraft.box.feature.gui.internal.mode;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import net.okocraft.box.feature.gui.internal.lang.DisplayKeys;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public final class StorageMode extends AbstractStorageMode {

    private final MiniMessageBase displayName;
    private final Arg1<Integer> leftClickToDeposit;
    private final Arg1<Integer> rightClickToWithdraw;
    private final Arg1<Integer> currentStock;

    private final MiniMessageBase depositAllDisplayName;
    private final MiniMessageBase depositAllLore;

    public StorageMode() {
        this.displayName = messageKey(DisplayKeys.STORAGE_MODE_DISPLAY_NAME);
        this.leftClickToDeposit = arg1(DisplayKeys.STORAGE_MODE_DEPOSIT, Placeholders.AMOUNT);
        this.rightClickToWithdraw = arg1(DisplayKeys.STORAGE_MODE_WITHDRAW, Placeholders.AMOUNT);
        this.currentStock = arg1(DisplayKeys.STORAGE_MODE_CURRENT_STOCK, Placeholders.CURRENT);
        this.depositAllDisplayName = messageKey(DisplayKeys.STORAGE_MODE_DEPOSIT_ALL_DISPLAY_NAME);
        this.depositAllLore = messageKey(DisplayKeys.STORAGE_MODE_DEPOSIT_ALL_LORE);
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName(@NotNull PlayerSession session) {
        return this.displayName.create(session.getMessageSource());
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = amountData != null ? amountData.getValue() : 1;

        return ItemEditor.create()
                .copyLoreFrom(item.getOriginal())
                .loreEmptyLine()
                .loreLine(this.leftClickToDeposit.apply(transactionAmount).create(session.getMessageSource()))
                .loreLine(this.rightClickToWithdraw.apply(transactionAmount).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.currentStock.apply(currentStock).create(session.getMessageSource()))
                .applyTo(item.getClonedItem());
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
        return new DepositAllButton(slot, this.depositAllDisplayName, this.depositAllLore);
    }

    private static class DepositAllButton extends AbstractDepositAllButton {

        private final MiniMessageBase displayName;
        private final MiniMessageBase lore;

        private DepositAllButton(int slot, @NotNull MiniMessageBase displayName, @NotNull MiniMessageBase lore) {
            super(
                    slot,
                    (session, clickType) -> clickType.isShiftClick(),
                    ClickResult.NO_UPDATE_NEEDED
            );
            this.displayName = displayName;
            this.lore = lore;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            return ItemEditor.create()
                    .displayName(this.displayName.create(session.getMessageSource()))
                    .loreLines(this.lore.create(session.getMessageSource()))
                    .createItem(Material.NETHER_STAR);
        }
    }
}
