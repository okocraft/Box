package net.okocraft.box.command.box;

import net.okocraft.box.command.BaseBoxCommand;

public abstract class BaseSubCommand extends BaseBoxCommand {

    @Override
    public String getPermissionNode() {
        return "box." + getCommandName();
    }
}