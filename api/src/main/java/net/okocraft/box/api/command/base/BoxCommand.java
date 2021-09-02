package net.okocraft.box.api.command.base;

import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.command.SubCommandHoldable;
import org.jetbrains.annotations.NotNull;

/**
 * An interface of /box command.
 */
public interface BoxCommand extends Command, SubCommandHoldable {

    /**
     * Changes the command when there is no argument.
     *
     * @param command the command to change
     */
    void changeNoArgumentCommand(@NotNull Command command);
}
