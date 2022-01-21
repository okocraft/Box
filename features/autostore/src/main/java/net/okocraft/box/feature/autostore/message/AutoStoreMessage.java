package net.okocraft.box.feature.autostore.message;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.message.argument.DoubleArgument;
import net.okocraft.box.api.message.argument.SingleArgument;
import net.okocraft.box.api.model.item.BoxItem;

import static net.okocraft.box.api.message.Components.aquaItemName;
import static net.okocraft.box.api.message.Components.aquaText;
import static net.okocraft.box.api.message.Components.aquaTranslatable;
import static net.okocraft.box.api.message.Components.commandHelp;
import static net.okocraft.box.api.message.Components.grayTranslatable;
import static net.okocraft.box.api.message.Components.greenTranslatable;
import static net.okocraft.box.api.message.Components.redTranslatable;

public final class AutoStoreMessage {

    public static final Component ERROR_FAILED_TO_LOAD_SETTINGS =
            redTranslatable("box.autostore.error.failed-to-load-settings");

    public static final Component RELOAD_SUCCESS = grayTranslatable("box.autostore.reloaded");

    public static final Component AUTO_STORE_ALL_MODE_NAME = aquaTranslatable("box.autostore.mode.all");

    public static final Component AUTO_STORE_ITEM_MODE_NAME = aquaTranslatable("box.autostore.mode.item");

    public static final Component ENABLED_NAME = greenTranslatable("box.autostore.enabled");

    public static final Component DISABLED_NAME = redTranslatable("box.autostore.disabled");

    public static final SingleArgument<Boolean> ENABLED_OR_DISABLED =
            enabled -> enabled ? ENABLED_NAME : DISABLED_NAME;

    public static final SingleArgument<Boolean> COMMAND_AUTOSTORE_TOGGLED =
            enabled -> grayTranslatable("box.autostore.command.autostore-toggled", ENABLED_OR_DISABLED.apply(enabled));

    public static final SingleArgument<Boolean> COMMAND_MODE_CHANGED =
            allMode -> grayTranslatable(
                    "box.autostore.command.mode-changed",
                    allMode ? AUTO_STORE_ALL_MODE_NAME : AUTO_STORE_ITEM_MODE_NAME
            );

    public static final SingleArgument<Boolean> COMMAND_PER_ITEM_ALL_TOGGLED =
            enabled -> grayTranslatable("box.autostore.command.item.all-toggled", ENABLED_OR_DISABLED.apply(enabled));

    public static final DoubleArgument<BoxItem, Boolean> COMMAND_PER_ITEM_ITEM_TOGGLED =
            (item, enabled) -> grayTranslatable(
                    "box.autostore.command.item.item-toggled",
                    aquaItemName(item), ENABLED_OR_DISABLED.apply(enabled)
            );

    public static final SingleArgument<String> COMMAND_MODE_NOT_FOUND =
            mode -> redTranslatable("box.autostore.command.mode-not-found", aquaText(mode));

    public static final SingleArgument<String> COMMAND_NOT_BOOLEAN =
            invalid -> redTranslatable("box.autostore.command.not-boolean", aquaText(invalid));

    public static final Component COMMAND_HELP_1 = commandHelp("box.autostore.command.help.toggle", false);

    public static final Component COMMAND_HELP_2 = commandHelp("box.autostore.command.help.all", false);

    public static final Component COMMAND_HELP_3 = commandHelp("box.autostore.command.help.item", false);
}
