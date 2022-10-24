package net.okocraft.box.feature.autostore.command;

import java.util.Optional;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AutoStoreCommand extends AbstractCommand {

    private final AutoStoreAllCommand allCommand = new AutoStoreAllCommand();
    private final AutoStoreItemCommand itemCommand = new AutoStoreItemCommand();
    private final AutoStoreDirectCommand directCommand = new AutoStoreDirectCommand();

    public AutoStoreCommand() {
        super("autostore", "box.command.autostore", Set.of("a", "as"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 1) { // never reach.
            return;
        }

        var setting = AutoStoreCommandUtil.getSettingOrSendError(sender);
        if (setting == null) {
            return;
        }

        // other sub commands
        if (args.length > 1) {
            var subCommand = matchSubCommand(args[1]);
            if (subCommand.isPresent()) {
                subCommand.get().runCommand(sender, args, setting);
                return;
            }
            // fall through for toggle.
        }

        Boolean toggleAutoStore;
        if (args.length == 1) {
            toggleAutoStore = !setting.isEnabled();
        } else {
            toggleAutoStore = AutoStoreCommandUtil.getBoolean(args[1]);
            if (toggleAutoStore == null) {
                sender.sendMessage(AutoStoreMessage.COMMAND_MODE_NOT_FOUND.apply(args[1]));
                return;
            }
        }

        sender.sendMessage(AutoStoreMessage.COMMAND_AUTOSTORE_TOGGLED.apply(toggleAutoStore));

        if (setting.isEnabled() != toggleAutoStore) { // setting changed
            setting.setEnabled(toggleAutoStore);
            AutoStoreCommandUtil.callEvent(setting);
        }
    }

    @Override
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player) || args.length < 2) {
            return Collections.emptyList();
        }

        if (args.length == 2) {
            return Stream.of("all", "item", "on", "off", "direct")
                    .filter(mode -> mode.startsWith(args[1].toLowerCase(Locale.ROOT)))
                    .collect(Collectors.toList());
        }

        return matchSubCommand(args[1])
                .map(cmd -> cmd.runTabComplete(sender, args))
                .orElse(Collections.emptyList());
    }

    private Optional<AutoStoreSubCommand> matchSubCommand(String nameOrAlias) {
        return Stream.of(allCommand, directCommand, itemCommand)
                .filter(command -> command.getName().charAt(0) == nameOrAlias.charAt(0))
                .findAny();
    }

    @Override
    public @NotNull Component getHelp() {
        return Component.text()
                .append(AutoStoreMessage.COMMAND_HELP_1).append(Component.newline())
                .append(AutoStoreMessage.COMMAND_HELP_2).append(Component.newline())
                .append(AutoStoreMessage.COMMAND_HELP_3).append(Component.newline())
                .append(AutoStoreMessage.COMMAND_HELP_4)
                .build();
    }
}
