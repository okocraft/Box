package net.okocraft.box.command.box;

import org.bukkit.command.CommandSender;

import java.util.List;

class Version extends BaseSubCommand {

    private static final String COMMAND_NAME = "version";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/box version";

    Version() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, String[] args) {
        if (validate(sender, args)) {
            return false;
        }
        sender.sendMessage(MESSAGE_CONFIG.getVersionInfo().replaceAll("%version%", INSTANCE.getVersion()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, String[] args) {
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
        return MESSAGE_CONFIG.getVersionDesc();
    }


    @Override
    boolean validate(CommandSender sender, String[] args) {
        return super.validate(sender, args);
    }
}