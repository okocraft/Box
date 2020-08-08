package net.okocraft.box.plugin.command.commands.boxadmin;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.command.AbstractCommand;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class BoxAdminCommand extends AbstractCommand {

    public BoxAdminCommand(@NotNull Box plugin) {
        super(
                plugin,
                "boxadmin",
                Collections.emptyList(),
                List.of(
                ),
                BoxPermission.BOX_ADMIN
        );
    }
}
