package net.okocraft.box.feature.autostore.command;

import net.kyori.adventure.text.Component;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.feature.autostore.message.AutoStoreMessage;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
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

        if (!(sender instanceof Player player)) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ONLY_PLAYER);
            return;
        }
        var container = AutoStoreSettingContainer.INSTANCE;
        if (!container.isLoaded(player)) {
            player.sendMessage(AutoStoreMessage.ERROR_FAILED_TO_LOAD_SETTINGS);
            return;
        }

        var setting = container.get(player);

        // process autostore toggle
        if (args.length == 1) {
            changeAutoStore(setting, !setting.isEnabled(), sender);
            return;
        } else {
            Boolean value = AutoStoreCommandUtil.getBoolean(args[1]);
            if (value != null) {
                changeAutoStore(setting, value, sender);
                return;
            }
        }

        var subCommand = matchSubCommand(args[1]);

        if (subCommand.isPresent()) {
            subCommand.get().runCommand(sender, args, setting);
        } else {
            if (!args[1].equalsIgnoreCase("help")) {
                sender.sendMessage(AutoStoreMessage.COMMAND_SUB_COMMAND_NOT_FOUND.apply(args[1]));
            }

            sender.sendMessage(getHelp());
        }
    }

    private void changeAutoStore(@NotNull AutoStoreSetting setting, boolean value, @NotNull CommandSender sender) {
        sender.sendMessage(AutoStoreMessage.COMMAND_AUTOSTORE_TOGGLED.apply(value));

        if (setting.isEnabled() != value) { // setting changed
            setting.setEnabled(value);
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

    private @NotNull Optional<AutoStoreSubCommand> matchSubCommand(@NotNull String nameOrAlias) {
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
