package net.okocraft.box.feature.bemode.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.bemode.lang.Displays;
import net.okocraft.box.feature.bemode.util.BEPlayerChecker;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class AbstractStorageMode implements BoxItemClickMode {

    protected static final String TRANSACTION_AMOUNT_NAME = "transaction-amount";

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public void applyIconMeta(@NotNull Player viewer, @NotNull BoxItem item, @NotNull ItemMeta target) {
        var newLore = Optional.ofNullable(target.lore()).map(ArrayList::new).orElseGet(ArrayList::new);

        var session = PlayerSession.get(viewer);
        var additionalLore = List.of(
                Component.empty(),
                getButtonInformationLore(session),
                Component.empty(),
                Displays.CURRENT_STOCK.apply(session.getStockHolder().getAmount(item))
        );

        newLore.addAll(TranslationUtil.render(additionalLore, viewer));

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
    public boolean canUse(@NotNull Player viewer) {
        return BEPlayerChecker.isBEPlayer(viewer);
    }

    protected abstract @NotNull Component getButtonInformationLore(@NotNull PlayerSession session);

    private static class DepositAllButton extends AdditionalButton implements RefreshableButton {

        private boolean clicked;

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
            target.displayName(TranslationUtil.render(Displays.DEPOSIT_ALL_BUTTON_DISPLAY_NAME, viewer));

            var lore = new ArrayList<Component>();

            lore.add(Component.empty());
            lore.add(TranslationUtil.render(Displays.DEPOSIT_ALL_BUTTON_LORE, viewer));

            if (clicked) {
                lore.add(TranslationUtil.render(Displays.DEPOSIT_ALL_BUTTON_CONFIRMATION, viewer));
            }

            lore.add(Component.empty());

            target.lore(lore);

            return target;
        }

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            if (clicked) {
                clicked = false;
            } else {
                clicked = true;
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

            clicked = false;

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 100f, 2.0f);
        }
    }
}
