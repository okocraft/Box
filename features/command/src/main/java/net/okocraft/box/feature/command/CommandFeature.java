package net.okocraft.box.feature.command;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.Command;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.message.DefaultMessageCollector;
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
import net.okocraft.box.feature.command.shared.SharedStockListCommand;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class CommandFeature extends AbstractBoxFeature {

    private final List<Command> boxSubCommands;
    public final List<Command> boxAdminSubCommands;

    public CommandFeature(@NotNull FeatureContext.Registration context) {
        super("command");

        var sharedStockListCommand = new SharedStockListCommand(context.defaultMessageCollector());

        this.boxSubCommands = createCommands(context,
                DepositCommand::new, WithdrawCommand::new, GiveCommand::new,
                ItemInfoCommand::new, collector -> new StockListCommand(collector, sharedStockListCommand)
        );

        this.boxAdminSubCommands = createCommands(context,
                collector -> new StockCommand(collector, sharedStockListCommand),
                InfinityCommand::new, RegisterCommand::new, RenameCommand::new,
                ReloadCommand::new, ResetAllCommand::new, VersionCommand::new
        );
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        var api = BoxAPI.api();
        this.boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::register);
        this.boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::register);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        var api = BoxAPI.api();
        this.boxSubCommands.forEach(api.getBoxCommand().getSubCommandHolder()::unregister);
        this.boxAdminSubCommands.forEach(api.getBoxAdminCommand().getSubCommandHolder()::unregister);
    }

    @SafeVarargs
    private static @NotNull List<Command> createCommands(@NotNull FeatureContext.Registration context, @NotNull Function<DefaultMessageCollector, Command> @NotNull... factories) {
        return Stream.of(factories).map(func -> func.apply(context.defaultMessageCollector())).toList();
    }
}
