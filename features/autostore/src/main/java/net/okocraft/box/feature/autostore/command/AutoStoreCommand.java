package net.okocraft.box.feature.autostore.command;

import com.github.siroshun09.messages.minimessage.arg.Arg1;
import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.kyori.adventure.text.Component;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.ErrorMessages;
import net.okocraft.box.api.message.Placeholders;
import net.okocraft.box.feature.autostore.AutoStoreSettingProvider;
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

    private static final String DEFAULT_HELP = """
            <aqua>/box autostore [on/off]<dark_gray> - <gray>Switches on/off auto-store
            <aqua>/box autostore [all/item]<dark_gray> - <gray>Changes auto-store mode
            <aqua>/box autostore item <item> [on/off]<dark_gray> - <gray>Toggles the auto-store setting of the item
            <aqua>/box autostore direct [on/off]<dark_gray> - <gray>Toggles auto-store setting to store drops directly""";

    private final AutoStoreSettingProvider container;
    private final MiniMessageBase loadErrorMessage;
    private final AutoStoreAllCommand allCommand;
    private final AutoStoreItemCommand itemCommand;
    private final AutoStoreDirectCommand directCommand;
    private final MiniMessageBase help;
    private final Arg1<String> subCommandNotFound;

    public AutoStoreCommand(@NotNull AutoStoreSettingProvider container, @NotNull MiniMessageBase loadErrorMessage, @NotNull DefaultMessageCollector collector) {
        super("autostore", "box.command.autostore", Set.of("a", "as"));
        this.container = container;
        this.loadErrorMessage = loadErrorMessage;

        AutoStoreCommandUtil.addToggleMessages(collector);

        this.allCommand = new AutoStoreAllCommand(collector);
        this.itemCommand = new AutoStoreItemCommand(collector);
        this.directCommand = new AutoStoreDirectCommand(collector);

        AutoStoreCommandUtil.addErrorMessages(collector);
        this.help = MiniMessageBase.messageKey(collector.add("box.autostore.command.help", DEFAULT_HELP));
        this.subCommandNotFound = Arg1.arg1(collector.add("box.autostore.command.error.subcommand-not-found", "<red>Auto-store sub command named <aqua><arg><red> is not found."), Placeholders.ARG);
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        var msgSrc = BoxAPI.api().getMessageProvider().findSource(sender);

        if (!(sender instanceof Player player)) {
            ErrorMessages.COMMAND_ONLY_PLAYER.source(msgSrc).send(sender);
            return;
        }

        var setting = this.container.getIfLoaded(player.getUniqueId());

        if (setting == null) {
            this.loadErrorMessage.source(msgSrc).send(sender);
            return;
        }

        // process autostore toggle
        if (args.length == 1) {
            AutoStoreCommandUtil.changeAutoStore(setting, sender, msgSrc, !setting.isEnabled(), true);
            return;
        } else {
            Boolean value = AutoStoreCommandUtil.getBoolean(args[1]);
            if (value != null) {
                AutoStoreCommandUtil.changeAutoStore(setting, sender, msgSrc, value, true);
                return;
            }
        }

        var subCommand = matchSubCommand(args[1]);

        if (subCommand.isPresent()) {
            subCommand.get().runCommand(sender, args, msgSrc, setting);
        } else {
            if (!args[1].equalsIgnoreCase("help")) {
                this.subCommandNotFound.apply(args[1]).source(msgSrc).send(sender);
            }

            sender.sendMessage(this.getHelp(msgSrc));
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
        return Stream.of(this.allCommand, this.directCommand, this.itemCommand)
                .filter(command -> command.getName().charAt(0) == nameOrAlias.charAt(0))
                .findAny();
    }

    @Override
    public @NotNull Component getHelp(@NotNull MiniMessageSource msgSrc) {
        return this.help.create(msgSrc);
    }
}
