package net.okocraft.box.feature.autostore.command;

import java.util.Collections;
import java.util.List;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class AutoStoreAllCommand extends AutoStoreSubCommand {

    public AutoStoreAllCommand() {
        super("all");
    }

    @Override
    public void runCommand(@NotNull CommandSender sender, String[] args, @NotNull AutoStoreSetting setting) {
        sender.sendMessage(AutoStoreMessage.COMMAND_MODE_CHANGED.apply(true));

        if (AutoStoreCommandUtil.enableAutoStore(setting, sender) || !setting.isAllMode()) { // setting changed
            setting.setAllMode(true);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    List<String> runTabComplete(CommandSender sender, String[] args) {
        return Collections.emptyList();
    }
}
