package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.feature.autostore.gui.buttons.BulkEditingButton;
import net.okocraft.box.feature.autostore.gui.buttons.DirectButton;
import net.okocraft.box.feature.autostore.gui.buttons.ModeButton;
import net.okocraft.box.feature.autostore.gui.buttons.ToggleButton;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.button.Button;
import net.okocraft.box.feature.gui.api.buttons.BackButton;
import net.okocraft.box.feature.gui.api.menu.AbstractMenu;
import net.okocraft.box.feature.gui.api.menu.Menu;
import net.okocraft.box.feature.gui.api.menu.RenderedButton;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoStoreSettingMenu extends AbstractMenu {

    private final List<Button> buttons;

    AutoStoreSettingMenu(@NotNull AutoStoreSetting setting, @NotNull Menu backTo) {
        this.buttons = buttonConstructors().map(constructor -> constructor.apply(setting)).collect(Collectors.toCollection(ArrayList::new));
        this.buttons.add(new BackButton(backTo, 22));
    }

    @Override
    public int getRows() {
        return 3;
    }

    @Override
    public @NotNull Component getTitle() {
        return AutoStoreMenuDisplays.AUTOSTORE_MODE_SETTING_MENU_TITLE;
    }

    @Override
    public boolean shouldUpdate() {
        return true;
    }

    @Override
    public void updateMenu(@NotNull Player viewer) {
        buttons.stream()
                .map(RenderedButton::create)
                .peek(button -> button.updateIcon(viewer))
                .forEach(this::addButton);
    }

    private @NotNull Stream<Function<AutoStoreSetting, Button>> buttonConstructors() {
        return Stream.of(ModeButton::new, BulkEditingButton::new, ToggleButton::new, DirectButton::new);
    }
}
