package net.okocraft.box.config;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;

import net.okocraft.box.Box;

public final class Messages extends CustomConfig {
    
    public Messages() {
        super("messages.yml");
    }

    /**
     * Send message to player.
     * 
     * @param player
     * @param addPrefix
     * @param path
     * @param placeholders
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path, Map<String, Object> placeholders) {
        String prefix = addPrefix ? get().getString("command.general.info.plugin-prefix", "&8[&6Box&8]&r") + " " : "";
        String message = prefix + getMessage(path);
        for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
            message = message.replace(placeholder.getKey(), placeholder.getValue().toString());
        }
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        return;
    }

    /**
     * Send message to player.
     * 
     * @param player
     * @param path
     * @param placeholders
     */
    public void sendMessage(CommandSender sender, String path, Map<String, Object> placeholders) {
        sendMessage(sender, true, path, placeholders);
    }

    /**
     * Send message to player.
     * 
     * @param sender
     * @param path
     */
    public void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, Map.of());
    }

    /**
     * Send message to player.
     * 
     * @param sender
     * @param addPrefix
     * @param path
     */
    public void sendMessage(CommandSender sender, boolean addPrefix, String path) {
        sendMessage(sender, addPrefix, path, Map.of());
    }

    /**
     * Gets message from key. Returned messages will not translated its color code.
     * 
     * @param path
     * @return
     */
    public String getMessage(String path) {
        return get().getString(path, path);
    }

    public void sendDisabledWorld(CommandSender sender) {
        sendMessage(sender, "command.general.error.in-disabled-world");
    }

    public void sendNotEnoughArguments(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-arguments");
    }

    public void sendInvalidArgument(CommandSender sender, String argument) {
        sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", argument));
    }

    public void sendAutoStore(CommandSender sender, String itemName, boolean switchTo) {
        sendMessage(sender, "command.box.auto-store.info.changed",
                Map.of("%item%", itemName, "%is-enabled%", String.valueOf(switchTo)));
    }

    public void sendAutoStoreAll(CommandSender sender, boolean switchTo) {
        sendMessage(sender, "command.box.auto-store.info.changed-all",
                Map.of("%is-enabled%", String.valueOf(switchTo)));
    }

    public void sendUnknownError(CommandSender sender) {
        sendMessage(sender, "command.general.error.unknown-exception");
    }

    public void sendItemNotFound(CommandSender sender) {
        sendMessage(sender, "command.general.error.item-not-found");
    }

    public void sendAutoStoreListHeader(CommandSender sender, String player, int page, int currentLine, int maxLine) {
        sendMessage(sender, "command.box.auto-store-list.info.header", Map.of(
                "%player%", player,
                "%page%", String.valueOf(page),
                "%current-line%", String.valueOf(currentLine),
                "%max-line%", String.valueOf(maxLine))
        );
    }

    public void sendAutoStoreListFormat(CommandSender sender, ItemStack item, boolean isEnabled) {
        sendMessage(sender, false, "command.box.auto-store-list.info.format", Map.of(
            "%item%", Box.getInstance().getAPI().getItemData().getName(item),
            "%is-enabled%", String.valueOf(isEnabled)
        ));
    }

    public void sendGiveInfoToSender(CommandSender sender, String given, String itemName, long amount, long newAmount) {
        sendMessage(sender, "command.box.give.info.sender", Map.of(
            "%player%", given,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendGiveInfoToTarget(CommandSender target, String giver, String itemName, long amount, long newAmount) {
        sendMessage(target, "command.box.give.info.player", Map.of(
            "%sender%", giver,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendAdminGiveInfoToSender(CommandSender sender, String given, String itemName, long amount, long newAmount) {
        sendMessage(sender, "command.boxadmin.give.info.sender", Map.of(
            "%player%", given,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendAdminGiveInfoToTarget(CommandSender target, String giver, String itemName, long amount, long newAmount) {
        sendMessage(target, "command.boxadmin.give.info.player", Map.of(
            "%sender%", giver,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendNotEnoughStock(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-stock");
    }

    public void sendCannotGiveMyself(CommandSender sender) {
        sendMessage(sender, "command.box.give.error.cannot-give-myself");
    }

    public void sendPlayerOnly(CommandSender sender) {
        sendPlayerOnly(sender);
    }

    public void sendPlayerNotFound(CommandSender sender) {
        sendMessage(sender, "command.general.error.player-not-found");
    }

    public void sendHelpHeader(CommandSender sender) {
        sendMessage(sender, "command.box.help.info.header");
    }

    public void sendHelpFormat(CommandSender sender, String command, String description) {
        sendMessage(sender, false, "command.box.help.info.format", Map.of("%command%", command, "%description%", description));
    }

    public void sendVersion(CommandSender sender, String version) {
        sendMessage(sender, "command.box.version.info.format", Map.of("%version%", version));
    }

    public void sendChooseChest(CommandSender sender) {
        sendMessage(sender, "command.boxadmin.add-category.info.choose-chest");
    }

    public void sendReloadSuccess(CommandSender sender) {
        sendMessage(sender, "command.boxadmin.reload.info.success");
    }

    public void sendSetInfoToSender(CommandSender sender, String target, String itemName, long amount) {
        sendMessage(sender, "command.boxadmin.set.info.sender", Map.of(
            "%player%", target,
            "%item%", itemName,
            "%amount%", String.valueOf(amount)
        ));
    }

    public void sendSetInfoToTarget(CommandSender target, String sender, String itemName, long amount) {
        sendMessage(target, "command.boxadmin.set.info.player", Map.of(
            "%sender%", sender,
            "%item%", itemName,
            "%amount%", String.valueOf(amount)
        ));
    }

    public void sendTakeInfoToSender(CommandSender sender, String target, String itemName, long amount, long newAmount) {
        sendMessage(sender, "command.boxadmin.take.info.sender", Map.of(
            "%player%", target,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendTakeInfoToTarget(CommandSender target, String sender, String itemName, long amount, long newAmount) {
        sendMessage(target, "command.boxadmin.give.info.player", Map.of(
            "%sender%", sender,
            "%item%", itemName,
            "%amount%", String.valueOf(amount),
            "%new-amount%", newAmount
        ));
    }

    public void sendNoPermission(CommandSender sender, String permission) {
        sendMessage(sender, false, "command.general.error.no-permission", Map.of("%permission%", permission));
    }

    public void sendUsage(CommandSender sender, String usage) {
        sendMessage(sender, false, "command.general.info.usage", Map.of("%usage%", usage));
    }
}