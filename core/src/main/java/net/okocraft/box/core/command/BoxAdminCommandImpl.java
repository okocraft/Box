package net.okocraft.box.core.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.message.Components;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BoxAdminCommandImpl extends BaseCommand implements BoxAdminCommand {

    @Override
    public @NotNull String getName() {
        return "boxadmin";
    }

    @Override
    public @NotNull String getPermissionNode() {
        return "box.admin.command";
    }

    @Override
    public @NotNull @Unmodifiable Set<String> getAliases() {
        return Set.of("ba", "badmin");
    }

    @Override
    public @NotNull Component getHelp() {
        return Components.commandHelp("box.command.boxadmin");
    }
}
