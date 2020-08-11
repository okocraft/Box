package net.okocraft.box.plugin.command.commands.box;

import net.md_5.bungee.api.chat.BaseComponent;
import net.okocraft.box.plugin.BoxPermission;
import net.okocraft.box.plugin.command.AbstractSubCommand;
import net.okocraft.box.plugin.command.ArgumentList;
import net.okocraft.box.plugin.command.Command;
import net.okocraft.box.plugin.locale.formatter.ComponentGenerator;
import net.okocraft.box.plugin.locale.message.Message;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.result.CommandResult;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnegative;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DepositCommand extends AbstractSubCommand {

    public DepositCommand(@NotNull Command parent) {
        super(
                parent,
                "deposit",
                List.of("d"),
                BoxPermission.BOX_COMMAND_DEPOSIT,
                Message.USAGE_COMMAND_BOX_DEPOSIT
        );
    }

    @Override
    protected @NotNull CommandResult onCommand(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        Player player = (Player) sender;

        if (args.size() < 2) {
            return depositItemInMainHand(player, 64);
        }

        if (args.get(1).equalsIgnoreCase("all")) {
            return depositAllItems(player);
        }

        return depositSpecifiedItems(player, args);
    }

    @Override
    protected @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull ArgumentList args) {
        return null;
    }

    @NotNull
    private CommandResult depositItemInMainHand(@NotNull Player player, @Nonnegative int limit) {
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType().isAir()) {
            getPlugin().getLocaleLoader().format(Message.DEPOSIT_NO_ITEM_IN_MAIN, true).send(player);
            return CommandResult.STATE_ERROR;
        }

        Optional<Item> boxItem = getPlugin().getItemManager().getItem(item);

        if (boxItem.isEmpty()) {
            String name = item.getItemMeta() != null ? item.getItemMeta().getDisplayName() : item.getType().toString();
            getPlugin().getLocaleLoader().format(Message.ERROR_ITEM_NOT_FOUND, true, name);
            return CommandResult.STATE_ERROR;
        }

        int amount = Math.min(item.getAmount(), limit);

        User user = getPlugin().getUserManager().getUser(player.getUniqueId());
        int now = getPlugin().getDataHandler().increase(user, boxItem.get(), amount);

        player.getInventory().setItemInMainHand(null);

        sendDepositedMessage(player, boxItem.get(), amount, now);

        return CommandResult.SUCCESS;
    }

    @NotNull
    private CommandResult depositAllItems(@NotNull Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        Map<Item, Integer> added = new HashMap<>();

        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];

            if (item == null) {
                continue;
            }

            Optional<Item> boxItem = getPlugin().getItemManager().getItem(item);

            if (boxItem.isEmpty()) {
                continue;
            }

            int amount = item.getAmount();

            User user = getPlugin().getUserManager().getUser(player.getUniqueId());
            getPlugin().getDataHandler().increase(user, boxItem.get(), amount);

            player.getInventory().setItem(i, null);

            if (added.containsKey(boxItem.get())) {
                added.put(boxItem.get(), added.get(boxItem.get()) + amount);
            } else {
                added.put(boxItem.get(), amount);
            }
        }

        StringBuilder detailBuilder = new StringBuilder();
        for (Item item : added.keySet()) {
            detailBuilder.append(
                    getPlugin().getLocaleLoader().format(
                            Message.TRANSACTION_DETAIL_INCREASED, false, item.getName(), String.valueOf(added.get(item))
                    )
            ).append(System.lineSeparator());
        }

        BaseComponent component = ComponentGenerator.generate(
                getPlugin().getLocaleLoader().format(Message.DEPOSIT_ALL, true).asString(),
                0,
                ComponentGenerator.getHoverText(
                        getPlugin().getLocaleLoader().get(Message.TRANSACTION_DETAIL_HOVER_TEXT),
                        detailBuilder.toString()
                )
        );

        player.spigot().sendMessage(component);

        return CommandResult.SUCCESS;
    }

    @NotNull
    private CommandResult depositSpecifiedItems(@NotNull Player player, @NotNull ArgumentList args) {
        Optional<Item> optionalItem = getPlugin().getItemManager().getItemByName(args.get(1));

        int amount = 64;
        Item boxItem;

        if (optionalItem.isPresent()) {
            boxItem = optionalItem.get();
            if (2 < args.size()) {
                amount = Math.max(1, args.getInt(2));
            }
        } else {
            return depositItemInMainHand(player, Math.max(1, args.getIntOrDefault(1, 64)));
        }

        return CommandResult.SUCCESS; // TODO
    }

    private void sendDepositedMessage(@NotNull Player player, @NotNull Item boxItem, int deposited, int now) {
        String formatted =
                getPlugin().getLocaleLoader().format(Message.DEPOSIT_ITEM, true, "{0}", String.valueOf(deposited), String.valueOf(now)).asString();

        BaseComponent component = ComponentGenerator.generate(
                formatted,
                0,
                ComponentGenerator.getHoverItem(boxItem.getName(), boxItem.getOriginal())
        );

        player.spigot().sendMessage(component);
    }
}
