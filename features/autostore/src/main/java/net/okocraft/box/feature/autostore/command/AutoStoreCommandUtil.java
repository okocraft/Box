package net.okocraft.box.feature.autostore.command;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.feature.autostore.event.AutoStoreSettingChangeEvent;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AutoStoreCommandUtil {

    private AutoStoreCommandUtil() {}

    static boolean enableAutoStore(@NotNull AutoStoreSetting setting, @NotNull CommandSender sender) {
        if (!setting.isEnabled()) {
            setting.setEnabled(true);
            sender.sendMessage(AutoStoreMessage.COMMAND_AUTOSTORE_TOGGLED.apply(true));
            return true;
        } else {
            return false;
        }
    }

    static void callEvent(@NotNull AutoStoreSetting setting) {
        BoxProvider.get().getEventBus().callEventAsync(new AutoStoreSettingChangeEvent(setting));
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

}
