package net.okocraft.box.feature.gui.api.buttons.customnumber;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.gui.api.button.RefreshableButton;
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

@SuppressWarnings("ClassCanBeRecord")
public class DecreaseCustomNumberButton implements RefreshableButton {

    private final CustomNumberHolder holder;
    private final Component displayName;
    private final SingleArgument<Integer> clickToDecreaseLore;
    private final SingleArgument<Integer> currentAmountLore;
    private final int slot;
    private final Menu menuToUpdate;

    public DecreaseCustomNumberButton(@NotNull CustomNumberHolder holder, @NotNull Component displayName,
                                      @NotNull SingleArgument<Integer> clickToDecreaseLore,
                                      @NotNull SingleArgument<Integer> currentAmountLore,
                                      int slot, @Nullable Menu menuToUpdate) {
        this.holder = holder;
        this.displayName = displayName;
        this.clickToDecreaseLore = clickToDecreaseLore;
        this.currentAmountLore = currentAmountLore;
        this.slot = slot;
        this.menuToUpdate = menuToUpdate;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.RED_STAINED_GLASS_PANE;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(displayName, viewer));

        var lore = new ArrayList<Component>();

        var unit = holder.getUnit().getAmount();
        var currentAmount = holder.getAmount();

        lore.add(Component.empty());

        lore.add(clickToDecreaseLore.apply(unit));


        lore.add(Component.empty());

        lore.add(currentAmountLore.apply(currentAmount));

        target.lore(TranslationUtil.render(lore, viewer));

        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }

    @Override
    public void onClick(@NotNull Player clicker, @NotNull ClickType clickType) {
        var current = holder.getAmount();

        if (1 < current) {
            holder.decreaseAmount();
        } else {
            return;
        }

        clicker.playSound(clicker.getLocation(), Sound.BLOCK_WOODEN_BUTTON_CLICK_OFF, 100f, 1.5f);

        if (menuToUpdate != null) {
            menuToUpdate.updateMenu(clicker);
        }
    }
}
