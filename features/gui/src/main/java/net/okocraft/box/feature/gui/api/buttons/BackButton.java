package net.okocraft.box.feature.gui.api.buttons;

import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.util.TranslationUtil;
import net.okocraft.box.feature.gui.internal.lang.Displays;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BackButton extends MenuButton {

    private final int slot;

    public BackButton(@NotNull Menu menu, int slot) {
        super(() -> menu);
        this.slot = slot;
    }

    @Override
    public @NotNull Material getIconMaterial() {
        return Material.OAK_DOOR;
    }

    @Override
    public int getIconAmount() {
        return 1;
    }

    @Override
    public @Nullable ItemMeta applyIconMeta(@NotNull Player viewer, @NotNull ItemMeta target) {
        target.displayName(TranslationUtil.render(Displays.BACK_BUTTON, viewer));
        return target;
    }

    @Override
    public int getSlot() {
        return slot;
    }
}
