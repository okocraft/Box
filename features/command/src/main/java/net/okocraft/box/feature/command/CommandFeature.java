package net.okocraft.box.feature.command;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.feature.command.box.DepositCommand;
import net.okocraft.box.feature.command.box.GiveCommand;
import net.okocraft.box.feature.command.box.ItemInfoCommand;
import net.okocraft.box.feature.command.box.StockListCommand;
import net.okocraft.box.feature.command.box.WithdrawCommand;
import net.okocraft.box.feature.command.boxadmin.InfinityCommand;
import net.okocraft.box.feature.command.boxadmin.RegisterCommand;
import net.okocraft.box.feature.command.boxadmin.ReloadCommand;
import net.okocraft.box.feature.command.boxadmin.RenameCommand;
import net.okocraft.box.feature.command.boxadmin.ResetAllCommand;
import net.okocraft.box.feature.command.boxadmin.VersionCommand;
import net.okocraft.box.feature.command.boxadmin.stock.StockCommand;

import java.util.List;

public class CommandFeature extends AbstractBoxFeature {

    private final List<Command> boxSubCommands =
            List.of(new DepositCommand(), new GiveCommand(), new WithdrawCommand(),
                    new ItemInfoCommand(), new StockListCommand());

    public final List<Command> boxAdminSubCommands =
            List.of(new StockCommand(), new InfinityCommand(), new RegisterCommand(),
                    new RenameCommand(), new ReloadCommand(), new ResetAllCommand(),
                    new VersionCommand());

    public CommandFeature() {
        super("command");
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        var api = BoxAPI.api();
        boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::register);
        boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::register);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        var api = BoxAPI.api();
        boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::unregister);
        boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::unregister);
    }
}
