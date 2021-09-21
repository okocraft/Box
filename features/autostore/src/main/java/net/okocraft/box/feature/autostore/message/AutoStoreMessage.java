package net.okocraft.box.feature.autostore.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.autostore.model.mode.AutoStoreMode;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;
import static net.kyori.adventure.text.format.NamedTextColor.GREEN;
import static net.kyori.adventure.text.format.NamedTextColor.RED;

public final class AutoStoreMessage {

    public static final Component ERROR_FAILED_TO_LOAD_SETTINGS =
            translatable("box.autostore.error.failed-to-load-settings", RED);

    public static final Component RELOAD_SUCCESS =
            translatable("box.autostore.reloaded", GRAY);


    public static final SingleArgument<AutoStoreMode> AUTO_STORE_MODE_NAME =
            mode -> translatable("box.autostore.mode." + mode.getModeName());

    public static final SingleArgument<Boolean> ENABLED_NAME =
            bool -> bool ?
                    translatable("box.autostore.enabled", GREEN) :
                    translatable("box.autostore.disabled", RED);

    public static final SingleArgument<AutoStoreMode> COMMAND_MODE_CHANGED =
            mode ->
                    translatable()
                            .key("box.autostore.command.mode-changed")
                            .args(AUTO_STORE_MODE_NAME.apply(mode).color(AQUA))
                            .color(GRAY)
                            .build();

    public static final Component COMMAND_TIP_ALL_MODE_DISABLED =
            translatable("box.autostore.command.tip.all-mode-disabled", GRAY);

    public static final Component COMMAND_TIP_HOW_TO_TOGGLE_ALL_MODE =
            translatable()
                    .key("box.autostore.command.tip.how-to-toggle-all-mode")
                    .args(text("/box autostore all", AQUA))
                    .color(GRAY)
                    .build();

    public static final SingleArgument<Boolean> COMMAND_ALL_MODE_TOGGLED =
            enabled ->
                    translatable()
                            .key("box.autostore.command.all-toggled")
                            .args(ENABLED_NAME.apply(enabled))
                            .color(GRAY)
                            .build();

    public static final SingleArgument<Boolean> COMMAND_PER_ITEM_ALL_TOGGLED =
            enabled ->
                    translatable()
                            .key("box.autostore.command.per-item.all-toggled")
                            .args(ENABLED_NAME.apply(enabled))
                            .color(GRAY)
                            .build();

    public static final DoubleArgument<BoxItem, Boolean> COMMAND_PER_ITEM_ITEM_TOGGLED =
            (item, enabled) ->
                    translatable()
                            .key("box.autostore.command.per-item.item-toggled")
                            .args(
                                    item.getDisplayName().color(AQUA).hoverEvent(item.getOriginal()),
                                    ENABLED_NAME.apply(enabled)
                            )
                            .color(GRAY)
                            .build();

    public static final SingleArgument<String> COMMAND_MODE_NOT_FOUND =
            mode ->
                    translatable()
                            .key("box.autostore.command.mode-not-found")
                            .args(text(mode, AQUA))
                            .color(RED)
                            .build();

    public static final SingleArgument<String> COMMAND_NOT_BOOLEAN =
            invalid ->
                    translatable()
                            .key("box.autostore.command.not-boolean")
                            .args(text(invalid, AQUA))
                            .color(RED)
                            .build();

    public static final Component COMMAND_HELP_1 =
            translatable("box.autostore.command.help.toggle.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.autostore.command.help.toggle.description", GRAY));

    public static final Component COMMAND_HELP_2 =
            translatable("box.autostore.command.help.all.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.autostore.command.help.all.description", GRAY));

    public static final Component COMMAND_HELP_3 =
            translatable("box.autostore.command.help.peritem.command-line", AQUA)
                    .append(text(" - ", DARK_GRAY))
                    .append(translatable("box.autostore.command.help.peritem.description", GRAY));
}
