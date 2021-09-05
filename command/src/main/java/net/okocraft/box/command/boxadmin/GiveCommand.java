package net.okocraft.box.command.boxadmin;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.command.AbstractCommand;
import net.okocraft.box.api.message.GeneralMessage;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.command.message.BoxAdminMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.UUID;

public class GiveCommand extends AbstractCommand {

    public GiveCommand() {
        super("give", "box.command.admin.give", Set.of("g"));
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull String[] args) {
        if (!checkPermission(sender)) {
            return;
        }

        if (args.length < 4) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_NOT_ENOUGH_ARGUMENT);
            return;
        }

        try {
            var uuid = UUID.fromString(args[1]);
            processUUID(sender, uuid, args);
        } catch (IllegalArgumentException ignored) {
            processPlayerName(sender, args);
        }
    }

    private void processUUID(@NotNull CommandSender sender, @NotNull UUID uuid,
                             @NotNull String[] args) {
        var player = Bukkit.getPlayer(uuid);

        if (player != null) {
            var boxPlayer = BoxProvider.get().getBoxPlayerMap().get(player);
            processCommand(sender, boxPlayer.getUserStockHolder(), player, args);
            return;
        }

        var boxUser = BoxProvider.get().getUserManager().loadUser(uuid).join();
        var userStockHolder = BoxProvider.get().getStockManager().loadUserStock(boxUser).join();
        processCommand(sender, userStockHolder, null, args);
    }

    private void processPlayerName(@NotNull CommandSender sender, @NotNull String[] args) {
        var player = Bukkit.getPlayer(args[1]);

        if (player != null) {
            processPlayer(sender, player, args);
            return;
        }

        var boxUser = BoxProvider.get().getUserManager().search(args[1]).join();

        if (boxUser.isPresent()) {
            processBoxUser(sender, boxUser.get(), args);
        } else {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_PLAYER_NOT_FOUND.apply(args[1]));
        }
    }

    private void processPlayer(@NotNull CommandSender sender, @NotNull Player target,
                               @NotNull String[] args) {
        var boxPlayer = BoxProvider.get().getBoxPlayerMap().get(target);
        processCommand(sender, boxPlayer.getUserStockHolder(), target, args);
    }

    private void processBoxUser(@NotNull CommandSender sender, @NotNull BoxUser target,
                                @NotNull String[] args) {
        var userStockHolder = BoxProvider.get().getStockManager().loadUserStock(target).join();
        processCommand(sender, userStockHolder, null, args);
    }

    private void processCommand(@NotNull CommandSender sender, @NotNull UserStockHolder target,
                                @Nullable Player targetPlayer, @NotNull String[] args) {
        var item = BoxProvider.get().getItemManager().getBoxItem(args[2]);

        if (item.isEmpty()) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_ITEM_NOT_FOUND.apply(args[2]));
            return;
        }

        int amount;

        try {
            amount = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            sender.sendMessage(GeneralMessage.ERROR_COMMAND_INVALID_NUMBER.apply(args[3]));
            return;
        }

        int current = target.increase(item.get(), amount);

        sender.sendMessage(BoxAdminMessage.GIVE_SUCCESS_SENDER.apply(target.getName(), item.get(), amount, current));

        if (targetPlayer != null) {
            targetPlayer.sendMessage(BoxAdminMessage.GIVE_SUCCESS_TARGET.apply(sender.getName(), item.get(), amount, current));
        }
    }
}
