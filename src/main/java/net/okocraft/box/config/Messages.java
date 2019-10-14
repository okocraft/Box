package net.okocraft.box.config;

import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;

public final class Messages {

    private static CustomConfig messages = new CustomConfig("messages.yml");

    /**
     * Cannot use constructor.
     */
    private Messages() {
    }

    /**
     * Send message to player. The message will be their own language or English. To
     * add language, make yaml file named their own locale in languages folder, like
     * this {@code JailWorker/languages/en_us.yml}. {@code placeholder}'s key will be
     * replaced with its value.
     * 
     * @param player
     * @param addPrefix
     * @param path
     * @param placeholders
     * 
     * @see https://minecraft.gamepedia.com/Language
     */
    public static void sendMessage(CommandSender sender, boolean addPrefix, String path, Map<String, Object> placeholders) {
        String prefix = addPrefix ? get().getString("command.general.info.plugin-prefix", "&8[&6Box&8]&r") + " " : "";
        String message = ChatColor.translateAlternateColorCodes('&', prefix + getMessage(path));
        for (Map.Entry<String, Object> placeholder : placeholders.entrySet()) {
            message = message.replace(placeholder.getKey(), placeholder.getValue().toString());
        }
        sender.sendMessage(message);
        return;
    }

    /**
     * Send message to player. The message will be their own language or English. To
     * add language, make yaml file named their own locale in languages folder, like
     * this {@code JailWorker/languages/en_us.yml}. {@code placeholder}'s key will be
     * replaced with its value.
     * 
     * @param player
     * @param path
     * @param placeholders
     * 
     * @see https://minecraft.gamepedia.com/Language
     */
    public static void sendMessage(CommandSender sender, String path, Map<String, Object> placeholders) {
        sendMessage(sender, true, path, placeholders);
    }

    /**
     * Send message to player. The message will be their own language or English. To
     * add language, make yaml file named their own locale in languages folder, like
     * this {@code JailWorker/languages/en_us.yml}.
     * 
     * @param sender
     * @param path
     */
    public static void sendMessage(CommandSender sender, String path) {
        sendMessage(sender, path, Map.of());
    }

    /**
     * Send message to player. The message will be their own language or English. To
     * add language, make yaml file named their own locale in languages folder, like
     * this {@code JailWorker/languages/en_us.yml}.
     * 
     * @param sender
     * @param addPrefix
     * @param path
     */
    public static void sendMessage(CommandSender sender, boolean addPrefix, String path) {
        sendMessage(sender, addPrefix, path, Map.of());
    }

    public static String getMessage(String path) {
        return get().getString(path, path);
    }

    /**
     * Reload config. If this method used before {@code JailConfig.save()}, the
     * data on memory will be lost.
     */
    public static void reload() {
        messages.initConfig();
    }

    /**
     * Saves data on memory to yaml.
     */
    public static void save() {
        messages.saveConfig();
    }

    /**
     * Copies yaml from jar to data folder.
     */
    public static void saveDefault() {
        messages.saveDefaultConfig();
    }

    /**
     * Gets FileConfiguration of config.
     * 
     * @return config.
     */
    static FileConfiguration get() {
        return messages.getConfig();
    }
}