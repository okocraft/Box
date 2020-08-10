package net.okocraft.box.plugin.command.commands.box;

import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.command.AbstractCommand;
import net.okocraft.box.plugin.command.ArgumentList;
import net.okocraft.box.plugin.locale.message.Message;
import net.okocraft.box.plugin.result.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
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

    @Override
    public @NotNull CommandResult execute(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        if (sender instanceof Player) {
            return super.execute(sender, args);
        } else {
            plugin.getLocaleLoader().format(Message.ERROR_ONLY_PLAYER, true).send(sender);
            return CommandResult.NOT_PLAYER;
        }
    }
}
