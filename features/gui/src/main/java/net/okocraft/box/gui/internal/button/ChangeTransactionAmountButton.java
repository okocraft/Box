package net.okocraft.box.gui.internal.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.gui.api.button.RefreshableButton;
import net.okocraft.box.gui.api.menu.Menu;
import net.okocraft.box.gui.api.util.TransactionAmountHolder;
import net.okocraft.box.gui.api.util.TranslationUtil;
import net.okocraft.box.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressWarnings("ClassCanBeRecord")
public class ChangeTransactionAmountButton implements RefreshableButton {

    private final int slot;
    private final boolean increaseButton;
    private final Menu menuToUpdate;

    public ChangeTransactionAmountButton(int slot, boolean increaseButton,
                                         @Nullable Menu menuToUpdate) {
        this.slot = slot;
        this.increaseButton = increaseButton;
        this.menuToUpdate = menuToUpdate;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return increaseButton ? Material.LIGHT_BLUE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var displayName =
                increaseButton ?
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_DISPLAY_NAME :
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_DISPLAY_NAME;

        target.displayName(TranslationUtil.render(displayName, viewer));

        var unit = TransactionAmountHolder.getUnit(viewer).getAmount();
        var currentAmount = TransactionAmountHolder.getAmount(viewer);

        var lore = List.of(
                (increaseButton ?
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_INCREASE_LORE :
                        Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_DECREASE_LORE).apply(unit),
                Component.empty(),
                Displays.CHANGE_TRANSACTION_AMOUNT_BUTTON_CURRENT.apply(currentAmount)
        );

        target.lore(TranslationUtil.render(lore, viewer));

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var unit = TransactionAmountHolder.getUnit(clicker);

        if (increaseButton) {
            if (TransactionAmountHolder.getAmount(clicker) == 1 && unit.getAmount() != 1) {
                TransactionAmountHolder.set(clicker, unit.getAmount());
            } else {
                TransactionAmountHolder.increase(clicker, unit);
            }
        } else {
            TransactionAmountHolder.decrease(clicker, unit);
        }

        var sound = increaseButton ? Sound.BLOCK_WOODEN_BUTTON_CLICK_ON : Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF;
        clicker.playSound(clicker.getLocation(), sound, 100f, 1.5f);

        if (menuToUpdate != null) {
            menuToUpdate.updateMenu(clicker);
        }
    }
}
