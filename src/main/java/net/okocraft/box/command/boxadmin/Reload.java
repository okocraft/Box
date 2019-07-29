package net.okocraft.box.command.boxadmin;

import java.util.List;

import org.bukkit.command.CommandSender;

class Reload extends BaseSubAdminCommand {

    private static final String COMMAND_NAME = "reload";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/boxadmin reload";

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }
        INSTANCE.reloadConfig();

        CONFIG.reload();
        MESSAGE_CONFIG.reload();

        sender.sendMessage(MESSAGE_CONFIG.getConfigReloaded());

        return true;
    }

    @Override
    public List<String> runTabComplete(CommandSender sender, String[] args) {
        return List.of();
    }

    @Override
    public String getCommandName() {
        return COMMAND_NAME;
    }

    @Override
    public int getLeastArgLength() {
        return LEAST_ARG_LENGTH;
    }

    @Override
    public String getUsage() {
        return USAGE;
    }

    @Override
    public String getDescription() {
        return MESSAGE_CONFIG.getReloadDesc();
    }
}