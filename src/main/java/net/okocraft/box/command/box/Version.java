package net.okocraft.box.command.box;

import java.util.List;

import org.bukkit.command.CommandSender;

class Version extends BaseSubCommand {

    private static final String COMMAND_NAME = "version";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/box version";

    public Version() {
        super();
    }

    @Override
    public boolean runCommand(CommandSender sender, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }
        sender.sendMessage(MESSAGE_CONFIG.getVersionInfo().replaceAll("%version%", INSTANCE.getVersion()));
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
        return MESSAGE_CONFIG.getVersionDesc();
    }
}