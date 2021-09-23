package net.okocraft.box.feature.gui.api.buttons.customnumber;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
import net.okocraft.box.feature.gui.api.lang.Styles;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.session.CustomNumberHolder;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public class ChangeCustomNumberUnitButton implements RefreshableButton {

    private final CustomNumberHolder holder;
    private final Component displayName;
    private final Component clickToResetAmount;
    private final int slot;
    private final Menu menuToUpdate;

    public ChangeCustomNumberUnitButton(@NotNull CustomNumberHolder holder,
                                        @NotNull Component displayName, @NotNull Component clickToResetAmount,
                                        int slot, @Nullable Menu menuToUpdate) {
        this.holder = holder;
        this.displayName = displayName;
        this.clickToResetAmount = clickToResetAmount;
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
        target.displayName(TranslationUtil.render(displayName, viewer));

        var lore = new ArrayList<Component>();

        var currentUnit = holder.getUnit();

        for (var unit : CustomNumberHolder.Unit.values()) {
            lore.add(
                    Component.text()
                            .content(" > " + unit.getAmount())
                            .style(currentUnit == unit ? Styles.NO_DECORATION_AQUA : Styles.NO_DECORATION_GRAY)
                            .build()
            );
        }

        lore.add(Component.empty());
        lore.add(TranslationUtil.render(clickToResetAmount, viewer));

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
            if (holder.getAmount() != 1) {
                holder.setAmount(1);
                clicker.playSound(clicker.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 100f, 1.5f);
            }
        } else {
            holder.changeAmountUnit();
            clicker.playSound(clicker.getLocation(), Sound.BLOCK_COMPARATOR_CLICK, 100f, 1.5f);
        }

        if (menuToUpdate != null) {
            menuToUpdate.updateMenu(clicker);
        }
    }
}
