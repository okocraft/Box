package net.okocraft.box.feature.bemode.mode;

import dev.siroshun.mcmsgdef.MessageKey;
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

    private final MessageKey displayName;
    private final MessageKey.Arg1<Integer> clickToWithdraw;
    private final MessageKey.Arg1<Integer> currentStock;

    public StorageWithdrawMode(@NotNull MessageKey.Arg1<Integer> currentStock, @NotNull DefaultMessageCollector collector) {
        this.displayName = MessageKey.key(collector.add("box.bemode.storage-mode.withdraw.display-name", "<gray>Storage mode (withdraw)"));
        this.clickToWithdraw = MessageKey.arg1(collector.add("box.bemode.storage-mode.withdraw.click-to-withdraw", "<gray>Click to withdraw <aqua><amount><gray> items"), Placeholders.AMOUNT);
        this.currentStock = currentStock;
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
        ItemStack icon = item.getClonedItem();
        Amount amountData = session.getData(Amount.SHARED_DATA_KEY);

        return ItemEditor.create()
            .copyLoreFrom(icon)
            .loreEmptyLine()
            .loreLine(this.clickToWithdraw.apply(amountData != null ? amountData.getValue() : 1))
            .loreEmptyLine()
            .loreLine(this.currentStock.apply(session.getSourceStockHolder().getAmount(item)))
            .applyTo(session.getViewer(), icon);
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
        return this.processWithdraw(session, item);
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
