package net.okocraft.box.config;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.okocraft.box.Box;
import net.okocraft.box.util.ReflectionUtil;

public final class Messages extends CustomConfig {
    
    public Messages() {
        super("messages.yml");
    }

    /**
     * Send message compoent to player.
     * 
     * @param sender
     * @param addPrefix
     * @param path
     * @param placeholders
     */
    public void sendMessageComponent(CommandSender sender, boolean addPrefix, String path, Map<String, BaseComponent> placeholders) {
        String prefix = addPrefix ? get().getString("command.general.info.plugin-prefix", "&8[&6Box&8]&r") + " "
                : "";
        TextComponent message = new TextComponent();
        String rawMessage = ChatColor.translateAlternateColorCodes('&', prefix + getMessage(path));
        while (true) {
            int placeholderIndexFirst = rawMessage.indexOf("%");
            if (placeholderIndexFirst == -1) {
                message.addExtra(rawMessage);
                break;
            }
            message.addExtra(rawMessage.substring(0, placeholderIndexFirst));
            rawMessage = rawMessage.substring(placeholderIndexFirst + 1);
            int placeholderIndexSecond = rawMessage.indexOf("%");
            String key = "%" + rawMessage.substring(0, placeholderIndexSecond + 1);
            message.addExtra(placeholders.getOrDefault(key, new TextComponent(key)));
            rawMessage = rawMessage.substring(placeholderIndexSecond + 1);
        }

        sender.spigot().sendMessage(message);
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

    public void sendInventoryIsFull(CommandSender sender) {
        sendMessage(sender, "command.general.error.inventory-is-full");
    }

    public void sendNotEnoughArguments(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-arguments");
    }

    public void sendNotEnoughItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.not-enough-item");
    }

    public void sendInvalidArgument(CommandSender sender, String argument) {
        sendMessage(sender, "command.general.error.invalid-argument", Map.of("%argument%", argument));
    }

    public void sendAutoStore(CommandSender sender, ItemStack item, boolean switchTo) {
        sendMessageComponent(sender, true, "command.box.auto-store.info.changed",
                Map.of("%item%", toTextComponent(item), "%is-enabled%", new TextComponent(String.valueOf(switchTo))));
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
        sendMessageComponent(sender, false, "command.box.auto-store-list.info.format", Map.of(
            "%item%", toTextComponent(item),
            "%is-enabled%", new TextComponent(String.valueOf(isEnabled))
        ));
    }

    public void sendGiveInfoToSender(CommandSender sender, String given, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.box.give.info.sender", Map.of(
            "%player%", new TextComponent(given),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    public void sendGiveInfoToTarget(CommandSender target, String giver, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.box.give.info.player", Map.of(
            "%sender%", new TextComponent(giver),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    public void sendAdminGiveInfoToSender(CommandSender sender, String given, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.boxadmin.give.info.sender", Map.of(
            "%player%", new TextComponent(given),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    public void sendAdminGiveInfoToTarget(CommandSender target, String giver, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.boxadmin.give.info.player", Map.of(
            "%sender%", new TextComponent(giver),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
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

    public void sendSetInfoToSender(CommandSender sender, String target, ItemStack item, long amount) {
        sendMessageComponent(sender, true, "command.boxadmin.set.info.sender", Map.of(
            "%player%", new TextComponent(target),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount))
        ));
    }

    public void sendSetInfoToTarget(CommandSender target, String sender, ItemStack item, long amount) {
        sendMessageComponent(target, true, "command.boxadmin.set.info.player", Map.of(
            "%sender%", new TextComponent(sender),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount))
        ));
    }

    public void sendTakeInfoToSender(CommandSender sender, String target, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(sender, true, "command.boxadmin.take.info.sender", Map.of(
            "%player%", new TextComponent(target),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    public void sendTakeInfoToTarget(CommandSender target, String sender, ItemStack item, long amount, long newAmount) {
        sendMessageComponent(target, true, "command.boxadmin.give.info.player", Map.of(
            "%sender%", new TextComponent(sender),
            "%item%", toTextComponent(item),
            "%amount%", new TextComponent(String.valueOf(amount)),
            "%new-amount%", new TextComponent(String.valueOf(newAmount))
        ));
    }

    public void sendItemInfo(CommandSender sender, ItemStack item) {
        sendMessageComponent(sender, true, "command.box.iteminfo.info.format",
                Map.of("%item%", toTextComponent(item)));
    }

    public void sendNoPermission(CommandSender sender, String permission) {
        sendMessage(sender, "command.general.error.no-permission", Map.of("%permission%", permission));
    }

    public void sendUsage(CommandSender sender, String usage) {
        sendMessage(sender, "command.general.info.usage", Map.of("%usage%", usage));
    }

    public void sendItemNameChanged(CommandSender sender, String oldName, String newName) {
        sendMessage(sender, "command.boxadmin.customname.info.success", Map.of("%old%", oldName, "%new%", newName));
    }

    public void sendHoldItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.hold-item");
    }

    public void sendHoldItemOrSpecifyItem(CommandSender sender) {
        sendMessage(sender, "command.general.error.hold-item-or-specify-item");
    }

    public void sendItemRegistered(CommandSender sender, ItemStack item) {
        sendMessageComponent(sender, true, "command.boxadmin.register.info.success", Map.of("%item%", toTextComponent(item)));
    }

    public void sendWithdrawItem(CommandSender sender, ItemStack item, int amount, int stock) {
        sendMessageComponent(sender, true, "command.box.withdraw.info.success", Map.of("%item%", toTextComponent(item), "%amount%", new TextComponent(String.valueOf(amount)), "%stock%", new TextComponent(String.valueOf(stock))));
    }

    public void sendDepositItem(CommandSender sender, ItemStack item, int amount, int stock) {
        sendMessageComponent(sender, true, "command.box.deposit.info.success", Map.of("%item%", toTextComponent(item), "%amount%", new TextComponent(String.valueOf(amount)), "%stock%", new TextComponent(String.valueOf(stock))));
    }

    public void sendDepositItemAll(CommandSender sender) {
        sendMessage(sender, "command.box.deposit.info.all-success");
    }

    /**
     * Converts an {@link org.bukkit.inventory.ItemStack} to a Json string for
     * sending with {@link net.md_5.bungee.api.chat.BaseComponent}'s.
     *
     * @param itemStack the item to convert
     * @return the hover evnet that show the item
     */
    @Nullable
    private TextComponent toTextComponent(ItemStack itemStack) {
        // ItemStack methods to get a net.minecraft.server.ItemStack object for
        // serialization
        Class<?> craftItemStackClazz = ReflectionUtil.getOBCClass("inventory.CraftItemStack");
        Method asNMSCopyMethod = ReflectionUtil.getMethod(craftItemStackClazz, "asNMSCopy", ItemStack.class);

        // NMS Method to serialize a net.minecraft.server.ItemStack to a valid Json
        // string
        Class<?> nmsItemStackClazz = ReflectionUtil.getNMSClass("ItemStack");
        Class<?> nbtTagCompoundClazz = ReflectionUtil.getNMSClass("NBTTagCompound");
        Method saveNmsItemStackMethod = ReflectionUtil.getMethod(nmsItemStackClazz, "save", nbtTagCompoundClazz);

        // This will just be an empty NBTTagCompound instance to invoke the saveNms
        // method
        Object nmsNbtTagCompoundObj;

        // This is the net.minecraft.server.ItemStack object received from the asNMSCopy
        // method
        Object nmsItemStackObj;

        // This is the net.minecraft.server.ItemStack after being put through
        // saveNmsItem method
        Object itemAsJsonObject;

        try {
            nmsNbtTagCompoundObj = nbtTagCompoundClazz.getDeclaredConstructor().newInstance();
            nmsItemStackObj = asNMSCopyMethod.invoke(null, itemStack);
            itemAsJsonObject = saveNmsItemStackMethod.invoke(nmsItemStackObj, nmsNbtTagCompoundObj);
        } catch (Throwable t) {
            Box.getInstance().getLogger().log(Level.SEVERE, "failed to serialize itemstack to nms item", t);
            return null;
        }
        
        BaseComponent[] hoverEventComponents = new BaseComponent[]{
            new TextComponent(itemAsJsonObject.toString())
        };

        HoverEvent hover = new HoverEvent(HoverEvent.Action.SHOW_ITEM, hoverEventComponents);
        TextComponent text = new TextComponent(Box.getInstance().getAPI().getItemData().getName(itemStack));
        text.setHoverEvent(hover);
        return text;
    }
}