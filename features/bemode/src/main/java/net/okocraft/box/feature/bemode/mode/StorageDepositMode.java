package net.okocraft.box.feature.bemode.mode;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.session.TypedKey;
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class StorageDepositMode extends AbstractStorageMode {

    private final MiniMessageBase displayName;
    private final Arg1<Integer> currentStock;
    private final Arg1<Integer> clickToDeposit;

    private final MiniMessageBase depositAllDisplayName;
    private final MiniMessageBase depositAllLore;
    private final MiniMessageBase depositAllConfirmation;

    public StorageDepositMode(@NotNull Arg1<Integer> currentStock, @NotNull DefaultMessageCollector collector) {
        this.displayName = messageKey(collector.add("box.bemode.storage-mode.deposit.display-name", "<gray>Storage mode (deposit)"));
        this.clickToDeposit = Arg1.arg1(collector.add("box.bemode.storage-mode.deposit.click-to-deposit", "<gray>Click to deposit <aqua><amount><gray> items"), Placeholders.AMOUNT);
        this.depositAllDisplayName = messageKey(collector.add("box.bemode.storage-mode.deposit.deposit-all-button.display-name", "<gold>Deposits all items in inventory"));
        this.depositAllLore = messageKey(collector.add("box.bemode.storage-mode.deposit.deposit-all-button.lore", "<gray>Double click to deposit"));
        this.depositAllConfirmation = messageKey(collector.add("box.bemode.storage-mode.deposit.deposit-all-button.confirmation", "<gray>Please click again to confirm"));
        this.currentStock = currentStock;
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
        var icon = item.getClonedItem();
        var amountData = session.getData(Amount.SHARED_DATA_KEY);

        return ItemEditor.create()
            .copyLoreFrom(icon)
            .loreEmptyLine()
            .loreLine(this.clickToDeposit.apply(amountData != null ? amountData.getValue() : 1).create(session.getMessageSource()))
            .loreEmptyLine()
            .loreLine(this.currentStock.apply(session.getSourceStockHolder().getAmount(item)).create(session.getMessageSource()))
            .applyTo(icon);
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        return BEPlayerChecker.isBEPlayer(session.getViewer());
    }

    @Override
    public @NotNull ClickResult onSelect(@NotNull PlayerSession session) {
        return ClickResult.UPDATE_ICONS;
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        return this.processDeposit(session, item);
    }

    @Override
    public boolean hasAdditionalButton() {
        return true;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        return new DepositAllButton(slot, this.depositAllDisplayName, this.depositAllLore, this.depositAllConfirmation);
    }

    private static class DepositAllButton extends AbstractDepositAllButton {

        private static final TypedKey<Boolean> CLICKED = TypedKey.of(Boolean.class, "bemode_deposit_all_clicked");

        private static boolean isClicked(@NotNull PlayerSession session) {
            return session.getData(CLICKED) != null;
        }

        private static boolean canDepositAll(@NotNull PlayerSession session, @NotNull ClickType clickType) {
            if (session.removeData(CLICKED) != null) {
                return true;
            } else {
                session.putData(CLICKED, Boolean.TRUE);
                return false;
            }
        }

        private final MiniMessageBase displayName;
        private final MiniMessageBase lore;
        private final MiniMessageBase confirmation;

        private DepositAllButton(int slot, @NotNull MiniMessageBase displayName, @NotNull MiniMessageBase lore, @NotNull MiniMessageBase confirmation) {
            super(slot, DepositAllButton::canDepositAll, ClickResult.UPDATE_BUTTON);
            this.displayName = displayName;
            this.lore = lore;
            this.confirmation = confirmation;
        }

        @Override
        public @NotNull ItemStack createIcon(@NotNull PlayerSession session) {
            return ItemEditor.create()
                .displayName(this.displayName.create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.lore.create(session.getMessageSource()))
                .loreLineIf(isClicked(session), () -> this.confirmation.create(session.getMessageSource()))
                .loreEmptyLine()
                .createItem(Material.NETHER_STAR);
        }
    }
}
