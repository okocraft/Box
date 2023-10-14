package net.okocraft.box.feature.bemode.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.player.BoxPlayer;
import net.okocraft.box.feature.bemode.lang.Displays;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.button.ClickResult;
import net.okocraft.box.feature.gui.api.mode.AbstractStorageMode;
import net.okocraft.box.feature.gui.api.session.Amount;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageWithdrawMode extends AbstractStorageMode {

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_WITHDRAW_MODE_DISPLAY_NAME;
    }

    @Override
    public @NotNull ItemStack createItemIcon(@NotNull PlayerSession session, @NotNull BoxItem item) {
        var icon = item.getClonedItem();

        var newLore = Optional.ofNullable(icon.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        var additionalLore = List.of(
                Component.empty(),
                getButtonInformationLore(session),
                Component.empty(),
                Displays.CURRENT_STOCK.apply(session.getStockHolder().getAmount(item))
        );

        newLore.addAll(TranslationUtil.render(additionalLore, session.getViewer()));
        icon.lore(newLore);

        return icon;
    }

    @Override
    public boolean canUse(@NotNull Player viewer, @NotNull BoxPlayer source) {
        return BEPlayerChecker.isBEPlayer(viewer);
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

    private @NotNull Component getButtonInformationLore(@NotNull PlayerSession session) {
        var amountData = session.getData(Amount.SHARED_DATA_KEY);
        return Displays.STORAGE_WITHDRAW_MODE_CLICK_TO_WITHDRAW.apply(amountData != null ? amountData.getValue() : 1);
    }
}
