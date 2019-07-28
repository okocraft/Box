package net.okocraft.box.command.box;

import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class Version extends BaseSubCommand {

    private static final String COMMAND_NAME = "version";
    private static final int LEAST_ARG_LENGTH = 1;
    private static final String USAGE = "/box version";

    public Version() {
        super();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!validate(sender, args)) {
            return false;
        }
        sender.sendMessage(MESSAGE_CONFIG.getVersionInfo().replaceAll("%version%", INSTANCE.getVersion()));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
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
    protected boolean validate(CommandSender sender, String[] args) {
        if (!super.validate(sender, args)) {
            return false;
        }

        return true;
    }
}