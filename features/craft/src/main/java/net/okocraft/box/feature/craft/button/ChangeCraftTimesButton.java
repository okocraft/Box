package net.okocraft.box.feature.craft.button;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.util.CustomCraftTimes;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@SuppressWarnings("ClassCanBeRecord")
public class ChangeCraftTimesButton implements RefreshableButton {

    private final int slot;
    private final boolean increaseButton;
    private final Menu menuToUpdate;

    public ChangeCraftTimesButton(int slot, boolean increaseButton, @Nullable Menu menuToUpdate) {
        this.slot = slot;
        this.increaseButton = increaseButton;
        this.menuToUpdate = menuToUpdate;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return increaseButton ? Material.BLUE_STAINED_GLASS_PANE : Material.RED_STAINED_GLASS_PANE;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        var displayName =
                increaseButton ?
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_INCREASE_DISPLAY_NAME :
                        Displays.CHANGE_CRAFT_TIMES_BUTTON_DECREASE_DISPLAY_NAME;

        target.displayName(TranslationUtil.render(displayName, viewer));

        var lore = new ArrayList<Component>();

        var unit = CustomCraftTimes.getUnit(viewer).getAmount();
        var currentAmount = CustomCraftTimes.getAmount(viewer);

        lore.add(Component.empty());

        if (increaseButton) {
            if (unit != 1 && currentAmount == 1) {
                lore.add(Displays.CHANGE_CRAFT_TIMES_BUTTON_SET_TO_UNIT.apply(unit));
            } else {
                lore.add(Displays.CHANGE_CRAFT_TIMES_BUTTON_INCREASE_LORE.apply(unit));
            }
        } else {
            lore.add(Displays.CHANGE_CRAFT_TIMES_BUTTON_DECREASE_LORE.apply(unit));
        }

        lore.add(Component.empty());

        lore.add(Displays.CHANGE_CRAFT_TIMES_BUTTON_CURRENT.apply(currentAmount));

        target.lore(TranslationUtil.render(lore, viewer));

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var unit = CustomCraftTimes.getUnit(clicker);
        var current = CustomCraftTimes.getAmount(clicker);

        if (increaseButton) {
            if (current == 1 && unit.getAmount() != 1) {
                CustomCraftTimes.set(clicker, unit.getAmount());
            } else {
                CustomCraftTimes.increase(clicker, unit);
            }
        } else {
            if (1 < current) {
                CustomCraftTimes.decrease(clicker, unit);
            } else {
                return;
            }
        }

        var sound = increaseButton ? Sound.BLOCK_WOODEN_BUTTON_CLICK_ON : Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF;
        clicker.playSound(clicker.getLocation(), sound, 100f, 1.5f);

        if (menuToUpdate != null) {
            menuToUpdate.updateMenu(clicker);
        }
    }
}
