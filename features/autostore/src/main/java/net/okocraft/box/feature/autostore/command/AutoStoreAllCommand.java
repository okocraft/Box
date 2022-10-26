package net.okocraft.box.feature.autostore.command;

import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

class AutoStoreAllCommand extends AutoStoreSubCommand {

    AutoStoreAllCommand() {
        super("all");
    }

    @Override
    void runCommand(@NotNull CommandSender sender, @NotNull String[] args, @NotNull AutoStoreSetting setting) {
        sender.sendMessage(AutoStoreMessage.COMMAND_MODE_CHANGED.apply(true));

        if (AutoStoreCommandUtil.enableAutoStore(setting, sender) || !setting.isAllMode()) { // setting changed
            setting.setAllMode(true);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    @NotNull List<String> runTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
