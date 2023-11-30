package net.okocraft.box.core.command;

import net.okocraft.box.api.command.Command;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface CommandRegisterer {

    @Contract("_ -> this")
    default @NotNull CommandRegisterer register(@NotNull Command command) {
        this.registerCommand(command);
        return this;
    }

    void registerCommand(@NotNull Command command);

}
