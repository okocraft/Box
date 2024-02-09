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
import net.okocraft.box.feature.gui.api.util.ItemEditor;
import org.bukkit.Material;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class StorageWithdrawMode extends AbstractStorageMode {

    private final MiniMessageBase displayName;
    private final Arg1<Integer> clickToWithdraw;
    private final Arg1<Integer> currentStock;

    public StorageWithdrawMode(@NotNull Arg1<Integer> currentStock, @NotNull DefaultMessageCollector collector) {
        this.displayName = MiniMessageBase.messageKey(collector.add("box.bemode.storage-mode.withdraw.display-name", "<gray>Storage mode (withdraw)"));
        this.clickToWithdraw = Arg1.arg1(collector.add("box.bemode.storage-mode.withdraw.click-to-withdraw", "<gray>Click to withdraw <aqua><amount><gray> items"), Placeholders.AMOUNT);
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
                .loreLine(this.clickToWithdraw.apply(amountData != null ? amountData.getValue() : 1).create(session.getMessageSource()))
                .loreEmptyLine()
                .loreLine(this.currentStock.apply(session.getSourceStockHolder().getAmount(item)).create(session.getMessageSource()))
                .applyTo(icon);
    }

    @Override
    public boolean canUse(@NotNull PlayerSession session) {
        return BEPlayerChecker.isBEPlayer(session.getViewer());
    }

    @Override
    public void onSelect(@NotNull PlayerSession session) {
    }

    @Override
    public @NotNull ClickResult onClick(@NotNull PlayerSession session, @NotNull BoxItem item, @NotNull ClickType clickType) {
        return processWithdraw(session, item);
    }

    @Override
    public boolean hasAdditionalButton() {
        return false;
    }

    @Override
    public @NotNull Button createAdditionalButton(@NotNull PlayerSession session, int slot) {
        throw new UnsupportedOperationException();
    }
}
