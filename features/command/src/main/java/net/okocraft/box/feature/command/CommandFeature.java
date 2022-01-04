package net.okocraft.box.feature.command;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.feature.command.box.DepositCommand;
import net.okocraft.box.feature.command.box.GiveCommand;
import net.okocraft.box.feature.command.box.ItemInfoCommand;
import net.okocraft.box.feature.command.box.WithdrawCommand;
import net.okocraft.box.feature.command.boxadmin.InfinityCommand;
import net.okocraft.box.feature.command.boxadmin.RegisterCommand;
import net.okocraft.box.feature.command.boxadmin.ReloadCommand;
import net.okocraft.box.feature.command.boxadmin.RenameCommand;
import net.okocraft.box.feature.command.boxadmin.ResetCommand;
import net.okocraft.box.feature.command.boxadmin.StockModifyCommands;
import net.okocraft.box.feature.command.boxadmin.VersionCommand;
import net.okocraft.box.feature.command.boxadmin.stock.StockCommand;

import java.util.List;

public class CommandFeature extends AbstractBoxFeature {

    private final List<Command> boxSubCommands =
            List.of(new DepositCommand(), new GiveCommand(), new WithdrawCommand(),
                    new ItemInfoCommand());

    public final List<Command> boxAdminSubCommands =
            List.of(StockModifyCommands.give(), StockModifyCommands.set(), StockModifyCommands.take(),
                    new StockCommand(), new InfinityCommand(), new RegisterCommand(),
                    new RenameCommand(), new ReloadCommand(), new ResetCommand(),
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
