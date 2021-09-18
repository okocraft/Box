package net.okocraft.box.feature.craft.button;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.feature.craft.lang.Displays;
import net.okocraft.box.feature.craft.util.CustomCraftTimes;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Styles;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

@SuppressWarnings("ClassCanBeRecord")
public class ChangeUnitButton implements RefreshableButton {

    private final int slot;
    private final Menu menuToUpdate;

    public ChangeUnitButton(int slot, @Nullable Menu menuToUpdate) {
        this.slot = slot;
        this.menuToUpdate = menuToUpdate;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.WHITE_STAINED_GLASS_PANE;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.CHANGE_UNIT_BUTTON_DISPLAY_NAME, viewer));

        var lore = new ArrayList<Component>();

        var currentUnit = CustomCraftTimes.getUnit(viewer);

        for (var unit : CustomCraftTimes.Unit.values()) {
            lore.add(
                    Component.text()
                            .content(" > " + unit.getAmount())
                            .style(Styles.NO_STYLE)
                            .color(currentUnit == unit ? NamedTextColor.AQUA : NamedTextColor.GRAY)
                            .build()
            );
        }

        lore.add(Component.empty());
        lore.add(TranslationUtil.render(Displays.CHANGE_UNIT_BUTTON_SHIFT_CLICK_TO_RESET_TIMES, viewer));

        target.lore(lore);

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        if (clickType.isShiftClick()) {
            if (CustomCraftTimes.getAmount(clicker) != 1) {
                CustomCraftTimes.reset(clicker);

                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 1.5f);
            }
        } else {
            CustomCraftTimes.changeUnit(clicker);

            clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        }

        if (menuToUpdate != null) {
            menuToUpdate.updateMenu(clicker);
        }
    }
}
