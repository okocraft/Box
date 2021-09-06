package net.okocraft.box.command;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.command.box.DepositCommand;
import net.okocraft.box.command.box.GiveCommand;
import net.okocraft.box.command.box.WithdrawCommand;
import net.okocraft.box.command.boxadmin.RegisterCommand;
import net.okocraft.box.command.boxadmin.ReloadCommand;
import net.okocraft.box.command.boxadmin.RenameCommand;
import net.okocraft.box.command.boxadmin.StockModifyCommands;
import net.okocraft.box.command.boxadmin.VersionCommand;

import java.util.List;

public class CommandFeature extends AbstractBoxFeature {

    private final List<Command> boxSubCommands =
            List.of(new DepositCommand(), new GiveCommand(), new WithdrawCommand());

    public final List<Command> boxAdminSubCommands =
            List.of(new RegisterCommand(), new ReloadCommand(), new RenameCommand(),
                    StockModifyCommands.give(), StockModifyCommands.set(), StockModifyCommands.take(),
                    new VersionCommand());

    public CommandFeature() {
        super("command");
    }

    @Override
    public void enable() {
        var api = BoxProvider.get();
        boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::register);
        boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::register);
    }

    @Override
    public void disable() {
        var api = BoxProvider.get();
        boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::unregister);
        boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::unregister);
    }
}
