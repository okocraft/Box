package net.okocraft.box.feature.autostore.command;

import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AutoStoreDirectCommand extends AutoStoreSubCommand {

    AutoStoreDirectCommand() {
        super("direct");
    }

    @Override
    void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        boolean result;

        if (2 < args.length) {
            var bool = AutoStoreCommandUtil.getBoolean(args[2]);
            if (bool != null) {
                result = bool;
            } else {
                sender.sendMessage(AutoStoreMessage.COMMAND_NOT_BOOLEAN.apply(args[2]));
                return;
            }
        } else {
            result = !setting.isDirect();
        }

        sender.sendMessage(AutoStoreMessage.COMMAND_AUTOSTORE_DIRECT_TOGGLED.apply(result));

        if ((result && AutoStoreCommandUtil.enableAutoStore(setting, sender)) || result != setting.isDirect()) {
            setting.setDirect(result);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            return Stream.of("on", "off")
                    .filter(bool -> bool.startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
