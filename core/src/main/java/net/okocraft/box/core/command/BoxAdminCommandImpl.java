package net.okocraft.box.core.command;

import net.okocraft.box.api.command.base.BoxAdminCommand;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Set;

public class BoxAdminCommandImpl extends BaseCommand implements BoxAdminCommand {

    public BoxAdminCommandImpl(@NotNull BoxScheduler scheduler) {
        super(scheduler);
    }

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
}
