package net.okocraft.box.feature.autostore.command;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.feature.autostore.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class AutoStoreAllCommand extends AutoStoreSubCommand {

    private final MessageKey allModeEnabled;

    AutoStoreAllCommand(@NotNull DefaultMessageCollector collector) {
        super("all");
        this.allModeEnabled = MessageKey.key(collector.add("box.autostore.command.change-to-all-mode", "<gray>Auto-store mode is now <aqua>all-items<gray> and all items are stored to Box."));
    }

    @Override
    void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        sender.sendMessage(this.allModeEnabled);

        if (AutoStoreCommandUtil.changeAutoStore(setting, sender, true, false) || !setting.isAllMode()) { // setting changed
            setting.setAllMode(true);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
