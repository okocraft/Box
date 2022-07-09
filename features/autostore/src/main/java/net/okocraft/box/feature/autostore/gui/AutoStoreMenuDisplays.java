package net.okocraft.box.feature.autostore.gui;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;

import static net.kyori.adventure.text.Component.translatable;
import static net.okocraft.box.api.message.Components.blackTranslatable;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GOLD;
import static net.okocraft.box.feature.gui.api.lang.Styles.NO_DECORATION_GRAY;

public class AutoStoreMenuDisplays {

    static final Component AUTOSTORE_MODE_DISPLAY_NAME =
            translatable("box.autostore.gui.mode.display-name");

    static final SingleArgument<Boolean> AUTOSTORE_MODE_LORE =
            enabled ->
                    translatable()
                            .key("box.autostore.gui.mode.lore")
                            .args(AutoStoreMessage.ENABLED_OR_DISABLED.apply(enabled))
                            .style(NO_DECORATION_GRAY)
                            .build();

    static final Component AUTOSTORE_MODE_SETTING_MENU_TITLE = blackTranslatable("box.autostore.gui.setting-menu.title");

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_MODE =
            translatable("box.autostore.gui.setting-menu.change-mode.display-name", NO_DECORATION_GOLD);

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_ALL =
            translatable("box.autostore.gui.setting-menu.change-mode.all", NO_DECORATION_GRAY);

    static final Component AUTOSTORE_MODE_SETTING_MENU_CHANGE_TO_PER_ITEM =
            translatable("box.autostore.gui.setting-menu.change-mode.item", NO_DECORATION_GRAY);

    static final Component AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_TITLE =
            translatable("box.autostore.gui.setting-menu.bulk-editing.title", NO_DECORATION_GOLD);

    static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_CLICK =
            enabled ->
                    translatable()
                            .key("box.autostore.gui.setting-menu.bulk-editing.click")
                            .args(AutoStoreMessage.ENABLED_OR_DISABLED.apply(enabled))
                            .style(NO_DECORATION_GRAY)
                            .build();

    static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_DOUBLE_CLICK =
            enabled ->
                    translatable()
                            .key("box.autostore.gui.setting-menu.bulk-editing.double-click")
                            .args(AutoStoreMessage.ENABLED_OR_DISABLED.apply(enabled))
                            .style(NO_DECORATION_GRAY)
                            .build();

    static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_BULK_EDITING_RECENT =
            enabled ->
                    translatable()
                            .key("box.autostore.gui.setting-menu.bulk-editing.recent")
                            .args(AutoStoreMessage.ENABLED_OR_DISABLED.apply(enabled))
                            .style(NO_DECORATION_GRAY)
                            .build();

    static final SingleArgument<Boolean> AUTOSTORE_MODE_SETTING_MENU_TOGGLE_BUTTON =
            enabled ->
                    translatable()
                            .key("box.autostore.gui.setting-menu.toggle-button")
                            .args(AutoStoreMessage.ENABLED_OR_DISABLED.apply(enabled))
                            .style(NO_DECORATION_GRAY)
                            .build();
}
