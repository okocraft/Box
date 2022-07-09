package net.okocraft.box.feature.gui.api.menu;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.gui.api.event.MenuClickEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractMenu implements Menu {

    private static final ItemStack AIR = new ItemStack(Material.AIR);

    protected final Map<Integer, RenderedButton> buttonMap = new HashMap<>(getRows() * 9, 1.0f);
    protected boolean updated = false;

    @Override
    public void applyIcons(@NotNull ItemStack[] target) {
        Arrays.fill(target, AIR);

        buttonMap.values().stream()
                .filter(Objects::nonNull)
                .filter(button -> button.getSlot() < target.length)
                .forEach(button -> target[button.getSlot()] = button.getIcon());
    }

    @Override
    public void clickMenu(@NotNull Player clicker, int slot, @NotNull ClickType clickType) {
        var button = buttonMap.get(slot);

        if (button == null) {
            return;
        }

        var event = new MenuClickEvent(clicker, this, button, clickType);

        if (BoxProvider.get().getEventBus().callEvent(event).isCancelled()) {
            return;
        }

        button.clickButton(clicker, clickType);

        if (button.shouldUpdate()) {
            button.updateIcon(clicker);
            updated = true;
        }
    }

    @Override
    public boolean isUpdated() {
        var current = updated;
        updated = false;
        return current;
    }

    protected void addButton(@NotNull RenderedButton renderedButton) {
        buttonMap.put(renderedButton.getSlot(), renderedButton);
    }
}
