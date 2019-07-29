package net.okocraft.box.command.boxadmin;

import org.bukkit.command.CommandSender;

import java.util.List;

class Reload extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "reload";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/boxadmin reload";

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }
        INSTANCE.reloadConfig();

        CONFIG.reload();
        MESSAGE_CONFIG.reload();

        sender.sendMessage(MESSAGE_CONFIG.getConfigReloaded());

        return true;
    }

    @Override
    public List<String> onTabComplete(String[] args) {
        return List.of();
    }

    @Override
    String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    String getUsage() {
        return USAGE;
    }

    @Override
    String getDescription() {
        return MESSAGE_CONFIG.getReloadDesc();
    }
}