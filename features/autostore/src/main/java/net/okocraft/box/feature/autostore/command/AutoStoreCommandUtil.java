package net.okocraft.box.feature.autostore.command;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AutoStoreCommandUtil {

    private static final String AUTOSTORE_ENABLED_KEY = "box.autostore.command.enable-autostore";
    private static final MiniMessageBase AUTOSTORE_ENABLED = MiniMessageBase.messageKey(AUTOSTORE_ENABLED_KEY);
    private static final String AUTOSTORE_DISABLED_KEY = "box.autostore.command.disable-autostore";
    private static final MiniMessageBase AUTOSTORE_DISABLED = MiniMessageBase.messageKey(AUTOSTORE_DISABLED_KEY);

    private static final String NOT_BOOLEAN_KEY = "box.autostore.command.error.not-boolean";
    static final Arg1<String> NOT_BOOLEAN = Arg1.arg1(NOT_BOOLEAN_KEY, Placeholders.ARG);

    static void addToggleMessages(@NotNull DefaultMessageCollector collector) {
        collector.add(AUTOSTORE_ENABLED_KEY, "<gray>Auto-store is now <green>enabled<gray>.");
        collector.add(AUTOSTORE_DISABLED_KEY, "<gray>Auto-store is now <red>disabled<gray>.");
    }

    static void addErrorMessages(@NotNull DefaultMessageCollector collector) {
        collector.add(NOT_BOOLEAN_KEY, "<aqua><arg><red> is not boolean. [on/off/true/false]");
    }

    static boolean changeAutoStore(@NotNull AutoStoreSetting setting, @NotNull CommandSender sender, @NotNull MiniMessageSource msgSrc, boolean newState, boolean alwaysSendMessage) {
        boolean change = setting.isEnabled() != newState;

        if (change) {
            setting.setEnabled(newState);
        }

        if (change || alwaysSendMessage) {
            (newState ? AUTOSTORE_ENABLED : AUTOSTORE_DISABLED).source(msgSrc).send(sender);
        }

        return change;
    }

    static void callEvent(@NotNull AutoStoreSetting setting) {
        BoxAPI.api().getEventCallers().async().call(new AutoStoreSettingChangeEvent(setting));
    }

    static @Nullable Boolean getBoolean(@NotNull String arg) {
        // for aliases: t = true, f = false, of = off
        if ((arg.length() < 5 && arg.charAt(0) == 't') || arg.equalsIgnoreCase("on")) {
            return true;
        } else if ((arg.length() < 6 && arg.charAt(0) == 'f') || (arg.length() < 4 && arg.startsWith("of"))) {
            return false;
        } else {
            return null;
        }
    }

    private AutoStoreCommandUtil() {
        throw new UnsupportedOperationException();
    }
}
