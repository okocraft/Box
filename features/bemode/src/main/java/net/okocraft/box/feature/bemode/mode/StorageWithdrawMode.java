package net.okocraft.box.feature.bemode.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.bemode.lang.Displays;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.AdditionalButton;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageWithdrawMode implements BoxItemClickMode {

    private static final String TRANSACTION_AMOUNT_NAME = "transaction-amount";

    @Override
    public @NotNull String getName() {
        return "storage-withdraw";
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_WITHDRAW_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        ClickModeRegistry.getStorageMode().processWithdraw(context);
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
        return false;
    }

    @Override
    public boolean canUse(@NotNull Player viewer) {
        return BEPlayerChecker.isBEPlayer(viewer);
    }

    @Override
    public @NotNull AdditionalButton createAdditionalButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        throw new UnsupportedOperationException();
    }

    protected @NotNull @Unmodifiable List<Component> createLore(@NotNull BoxItem item, @NotNull Player player) {
        var session = PlayerSession.get(player);

        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        return List.of(
                Displays.STORAGE_WITHDRAW_MODE_CLICK_TO_WITHDRAW.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }
}
