package net.okocraft.box.core.message;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.arg.Arg2;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.base.Placeholder;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.arg.Arg1.arg1;
import static com.github.siroshun09.messages.minimessage.arg.Arg2.arg2;
import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;
import static net.okocraft.box.api.message.Placeholders.ERROR;

public class CoreMessages {

    private static final String HELP_BOX = "box.core.command.help-header";
    private static final String ONLY_PLAYER = "box.core.command.error.only-player";
    private static final String INVALID_NUMBER = "box.core.command.error.invalid-number";
    private static final String ITEM_NOT_FOUND = "box.core.command.error.item-not-found";
    private static final String PLAYER_NOT_FOUND = "box.core.command.error.player-not-found";
    private static final String SUB_COMMAND_NOT_FOUND = "box.core.command.error.sub-command-not-found";
    private static final String NOT_ENOUGH_ARGUMENT = "box.core.command.error.not-enough-argument";
    private static final String COMMAND_EXECUTION_ERROR = "box.core.command.error.exception-occurred";

    private static final String NO_PERMISSION = "box.core.error.no-permission";
    private static final String CANNOT_USE_BOX = "box.core.error.cannot-use-box";
    private static final String LOAD_ERROR_ON_JOIN = "box.core.error.player-data.load-on-join";
    private static final String NOT_LOADED_SELF = "box.core.error.player-data.not-loaded.self";
    private static final String NOT_LOADED_OTHER = "box.core.error.player-data.not-loaded.other";
    private static final String LOADING_SELF = "box.core.error.player-data.loading.self";
    private static final String LOADING_OTHER = "box.core.error.player-data.loading.other";

    private static final String CONFIG_RELOADED = "box.core.reload.config";
    private static final String MESSAGES_RELOADED = "box.core.reload.messages";
    private static final String ERROR_RELOAD_CONFIG = "box.core.reload.error.config";
    private static final String ERROR_RELOAD_MESSAGES = "box.core.reload.error.messages";
    private static final String ERROR_RELOAD_FEATURE = "box.core.reload.error.feature";

    public static final Arg1<String> COMMAND_HELP_HEADER = arg1(HELP_BOX, Placeholder.component("command", Component::text));
    public static final Arg1<Throwable> COMMAND_EXECUTION_ERROR_MSG = arg1(COMMAND_EXECUTION_ERROR, ERROR);
    public static final MiniMessageBase LOAD_FAILURE_ON_JOIN = messageKey(LOAD_ERROR_ON_JOIN);
    public static final Arg1<String> CONFIG_RELOADED_MSG = arg1(CONFIG_RELOADED, Placeholder.component("filename", Component::text));
    public static final MiniMessageBase MESSAGE_RELOADED_MSG = messageKey(MESSAGES_RELOADED);
    public static final Arg2<String, Throwable> CONFIG_RELOAD_FAILURE = arg2(ERROR_RELOAD_CONFIG, Placeholder.component("filename", Component::text), ERROR);
    public static final Arg1<Throwable> MESSAGES_RELOAD_FAILURE = arg1(ERROR_RELOAD_MESSAGES, ERROR);
    public static final Arg2<BoxFeature, Throwable> FEATURE_RELOAD_FAILURE = arg2(ERROR_RELOAD_FEATURE, Placeholder.component("feature", feature -> Component.text(feature.getName())), ERROR);

    public static void addDefaultMessages(@NotNull DefaultMessageCollector collector) {
        collector.add(HELP_BOX, "<dark_gray>============================== <gold>Command helps for <command><dark_gray> ==============================");
        collector.add(ONLY_PLAYER, "<red>This command can only be executed by the player in the game.");
        collector.add(INVALID_NUMBER, "<aqua><arg><red> is not a valid number.");
        collector.add(ITEM_NOT_FOUND, "<red>The item <aqua><item_name><red> could not be found.");
        collector.add(PLAYER_NOT_FOUND, "<red>The player <aqua><player_name><red> could not be found.");
        collector.add(SUB_COMMAND_NOT_FOUND, "<red>Box could not recognize the specified subcommand.");
        collector.add(NOT_ENOUGH_ARGUMENT, "<red>The arguments are not enough.");
        collector.add(COMMAND_EXECUTION_ERROR, "<red>Failed to execute command. Error message: <white><error>");

        collector.add(NO_PERMISSION, "<gray>You don't have the permission: <aqua><permission>");
        collector.add(CANNOT_USE_BOX, "<red>You cannot use Box in your world.");
        collector.add(LOAD_ERROR_ON_JOIN, "<red>Failed to load the stock data. Please contact the administrator.");
        collector.add(NOT_LOADED_SELF, "<red>Your player data is not loaded. Please contact the administrator.");
        collector.add(NOT_LOADED_OTHER, "<red>Player <aqua><player_name><red>'s data is not loaded. Please contact the administrator.");
        collector.add(LOADING_SELF, "<red>Your player data is currently loading. Please wait a moment.");
        collector.add(LOADING_OTHER, "<red>Player <aqua><player_name><red>'s data is currently loading. Please wait a moment.");

        collector.add(CONFIG_RELOADED, "<gray><filename> has been reloaded.");
        collector.add(MESSAGES_RELOADED, "<gray>Language files have been reloaded.");
        collector.add(ERROR_RELOAD_CONFIG, "<red>Failed to reload <filename>. Error message: <white><error>");
        collector.add(ERROR_RELOAD_MESSAGES, "<red>Failed to reload messages. Error message: <white><error>");
        collector.add(ERROR_RELOAD_FEATURE, "<red>Failed to reload feature '<feature>'. Error message: <white><error>");
    }
}
