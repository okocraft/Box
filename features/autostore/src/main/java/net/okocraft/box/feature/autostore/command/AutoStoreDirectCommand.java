package net.okocraft.box.feature.autostore.command;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class AutoStoreDirectCommand extends AutoStoreSubCommand {

    private final MessageKey directModeEnabled;
    private final MessageKey directModeDisabled;

    AutoStoreDirectCommand(@NotNull DefaultMessageCollector collector) {
        super("direct");
        this.directModeEnabled = MessageKey.key(collector.add("box.autostore.command.direct-mode-enabled", "<gray>Direct-auto-store is now <green>enabled<gray>."));
        this.directModeDisabled = MessageKey.key(collector.add("box.autostore.command.direct-mode-disabled", "<gray>Direct-auto-store is now <red>disabled<gray>."));
    }

    @Override
    void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        boolean enabled;

        if (2 < args.length) {
            var bool = AutoStoreCommandUtil.getBoolean(args[2]);
            if (bool != null) {
                enabled = bool;
            } else {
                sender.sendMessage(AutoStoreCommandUtil.NOT_BOOLEAN.apply(args[2]));
                return;
            }
        } else {
            enabled = !setting.isDirect();
        }

        sender.sendMessage(enabled ? this.directModeEnabled : this.directModeDisabled);

        if ((enabled && AutoStoreCommandUtil.changeAutoStore(setting, sender, true, false)) || enabled != setting.isDirect()) {
            setting.setDirect(enabled);
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
