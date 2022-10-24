package net.okocraft.box.feature.autostore.command;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreDirectCommand extends AutoStoreSubCommand {

    public AutoStoreDirectCommand() {
        super("direct");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        var value = !setting.isDirect();
        if (args.length > 2) {
            var bool = AutoStoreCommandUtil.getBoolean(args[2]);
            if (bool != null) {
                value = bool;
            }
        }
        sender.sendMessage(AutoStoreMessage.COMMAND_AUTOSTORE_DIRECT_TOGGLED.apply(value));

        if ((value && AutoStoreCommandUtil.enableAutoStore(setting, sender)) || value != setting.isDirect()) {
            setting.setDirect(value);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    public @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length == 3) {
            return Stream.of("on", "off")
                    .filter(bool -> bool.startsWith(args[2].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
