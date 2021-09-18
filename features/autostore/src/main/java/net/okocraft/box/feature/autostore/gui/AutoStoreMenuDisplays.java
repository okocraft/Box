package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;

import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.BLACK;
import static net.kyori.adventure.text.format.NamedTextColor.GOLD;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_STYLE;

public class AutoStoreMenuDisplays {

    static final Component AUTOSTORE_MODE_DISPLAY_NAME =
            translatable("box.gui.modes.autostore-mode.display-name");

    static final Component AUTOSTORE_MODE_ENABLED =
            translatable("box.gui.modes.autostore-mode.enabled").color(GREEN);

    static final Component AUTOSTORE_MODE_DISABLED =
            translatable("box.gui.modes.autostore-mode.disabled").color(RED);

    static final SingleArgument<Boolean> AUTOSTORE_MODE_LORE =
            enabled ->
                    translatable()
                            .key("box.gui.modes.autostore-mode.lore")
                            .args(enabled ? AUTOSTORE_MODE_ENABLED : AUTOSTORE_MODE_DISABLED)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();

    static final Component AUTOSTORE_MODE_SETTING_MENU_TITLE =
            translatable("box.gui.modes.autostore-mode.setting-menu.title", BLACK);

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.display-name", NO_STYLE.color(GOLD));

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.all", NO_STYLE.color(GRAY));

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM =
            translatable("box.gui.modes.autostore-mode.setting-menu.change-mode.per-item", NO_STYLE.color(GRAY));

    static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE =
            translatable("box.gui.modes.autostore-mode.setting-menu.bulk-editing.title", NO_STYLE.color(GOLD));

    static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_LEFT_CLICK =
            translatable()
                    .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.left-click")
                    .args(AUTOSTORE_MODE_ENABLED)
                    .style(NO_STYLE)
                    .color(GRAY)
                    .build();

    static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RIGHT_CLICK =
            translatable()
                    .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.right-click")
                    .args(AUTOSTORE_MODE_DISABLED)
                    .style(NO_STYLE)
                    .color(GRAY)
                    .build();

    static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RECENT =
            enabled ->
                    translatable()
                            .key("box.gui.modes.autostore-mode.setting-menu.bulk-editing.recent")
                            .args(enabled ? AUTOSTORE_MODE_ENABLED : AUTOSTORE_MODE_DISABLED)
                            .style(NO_STYLE)
                            .color(GRAY)
                            .build();
}
