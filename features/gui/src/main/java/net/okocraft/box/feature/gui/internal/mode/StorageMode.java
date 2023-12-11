package net.okocraft.box.feature.gui.internal.mode;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.transaction.InventoryTransaction;
import net.okocraft.box.feature.gui.api.event.stock.GuiCauses;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.mode.AdditionalButton;
import net.okocraft.box.feature.gui.api.mode.BoxItemClickMode;
import net.okocraft.box.feature.gui.api.session.PlayerSession;
import net.okocraft.box.feature.gui.api.util.SoundBase;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class StorageMode implements BoxItemClickMode {

    private static final SoundBase DEPOSIT_SOUND = SoundBase.builder().sound(Sound.ENTITY_ITEM_PICKUP).build();
    private static final SoundBase WITHDRAW_SOUND = SoundBase.builder().sound(Sound.BLOCK_STONE_BUTTON_CLICK_ON).build();

    private static final String TRANSACTION_AMOUNT_NAME = "transaction-amount";

    @Override
    public @NotNull String getName() {
        return "storage";
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.CHEST;
    }

    @Override
    public @NotNull Component getDisplayName() {
        return Displays.STORAGE_MODE_DISPLAY_NAME;
    }

    @Override
    public void onClick(@NotNull Context context) {
        if (context.clickType().isLeftClick()) {
            processDeposit(context);
            return;
        }

        if (context.clickType().isRightClick()) {
            processWithdraw(context);
        }
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
    public final boolean canUse(@NotNull Player viewer) {
        return true; // This is the default mode, so it must always be available to the player.
    }

    @Override
    public @NotNull AdditionalButton createAdditionalButton(@NotNull Player viewer, @NotNull Menu currentMenu) {
        return new DepositAllButton();
    }

    private @NotNull @Unmodifiable List<Component> createLore(@NotNull BoxItem item, @NotNull Player player) {
        var session = PlayerSession.get(player);

        int currentStock = session.getStockHolder().getAmount(item);
        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        return List.of(
                Displays.STORAGE_MODE_LEFT_CLICK_TO_DEPOSIT.apply(transactionAmount),
                Displays.STORAGE_MODE_RIGHT_CLICK_TO_WITHDRAW.apply(transactionAmount),
                Component.empty(),
                Displays.STORAGE_MODE_CURRENT_STOCK.apply(currentStock)
        );
    }

    public void processDeposit(@NotNull Context context) {
        var player = context.clicker();
        var session = PlayerSession.get(player);

        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        var resultList =
                BoxProvider.get().getTaskFactory()
                        .supplyFromEntity(player, $player -> InventoryTransaction.depositItem($player.getInventory(), context.item(), transactionAmount))
                        .join();

        if (!resultList.getType().isModified()) {
            SoundBase.UNSUCCESSFUL.play(player);
            return;
        }

        var stockHolder = session.getStockHolder();

        resultList.getResultList()
                .stream()
                .filter(result -> result.getType().isModified())
                .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount(), new GuiCauses.Deposit(player)));

        DEPOSIT_SOUND.play(player);
    }

    public void processWithdraw(@NotNull Context context) {
        var player = context.clicker();
        var session = PlayerSession.get(player);

        var stockHolder = session.getStockHolder();
        var currentStock = stockHolder.getAmount(context.item());

        if (currentStock < 1) {
            SoundBase.UNSUCCESSFUL.play(player);
            return;
        }

        int transactionAmount = session.getCustomNumberHolder(TRANSACTION_AMOUNT_NAME).getAmount();

        var amount = Math.min(currentStock, transactionAmount);

        var result =
                BoxProvider.get().getTaskFactory()
                        .supplyFromEntity(player, $player -> InventoryTransaction.withdraw($player.getInventory(), context.item(), amount))
                        .join();

        if (result.getType().isModified()) {
            stockHolder.decrease(result.getItem(), result.getAmount(), new GuiCauses.Withdraw(player));
            WITHDRAW_SOUND.play(player);
        } else {
            SoundBase.UNSUCCESSFUL.play(player);
        }
    }

    private static class DepositAllButton extends AdditionalButton {

        private static final SoundBase DEPOSIT_ALL_SOUND = SoundBase.builder().sound(Sound.BLOCK_NOTE_BLOCK_HARP).pitch(2.0f).build();

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

        @SuppressWarnings("DuplicatedCode")
        @Override
        public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
            if (!clickType.isShiftClick()) {
                return;
            }

            var resultList =
                    BoxProvider.get().getTaskFactory()
                            .supplyFromEntity(clicker, player -> InventoryTransaction.depositItemsInInventory(player.getInventory()))
                            .join();

            if (!resultList.getType().isModified()) {
                SoundBase.UNSUCCESSFUL.play(clicker);
                return;
            }

            var stockHolder = PlayerSession.get(clicker).getStockHolder();

            resultList.getResultList()
                    .stream()
                    .filter(result -> result.getType().isModified())
                    .forEach(result -> stockHolder.increase(result.getItem(), result.getAmount(), new GuiCauses.Deposit(clicker)));

            DEPOSIT_ALL_SOUND.play(clicker);
        }
    }
}
