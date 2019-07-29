package net.okocraft.box.command.boxadmin;

import net.okocraft.box.command.BaseBoxCommand;

public abstract class BaseSubAdminCommand extends BaseBoxCommand {
    @Override
    public String getPermissionNode() {
        return "boxadmin." + getCommandName();
    }
}