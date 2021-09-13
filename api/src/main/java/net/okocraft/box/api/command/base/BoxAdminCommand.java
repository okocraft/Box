package net.okocraft.box.api.command.base;

import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An interface of /boxadmin command.
 */
public interface BoxAdminCommand extends Command, SubCommandHoldable {

    /**
     * Changes the command when there is no argument.
     *
     * @param command the command to change
     */
    void changeNoArgumentCommand(@Nullable Command command);
}
