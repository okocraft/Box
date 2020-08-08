package net.okocraft.box.plugin.command.commands.box;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.command.AbstractCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class BoxCommand extends AbstractCommand {

    public BoxCommand(@NotNull Box plugin) {
        super(
                plugin,
                "box",
                List.of("b", "okobox"),
                List.of(
                ),
                BoxPermission.BOX_COMMAND
        );
    }
}
