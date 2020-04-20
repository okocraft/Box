package net.okocraft.box.command;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import net.okocraft.box.Box;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;

public abstract class Commands implements CommandExecutor, TabCompleter {

    protected final Box plugin = Box.getInstance();
    protected final Config config = plugin.getAPI().getConfig();
    protected final Messages messages = plugin.getAPI().getMessages();
    protected final Map<String, BaseCommand> registeredSubCommands = new LinkedHashMap<>();

    protected void register(BaseCommand subCommand) {
        String commandName = subCommand.getName().toLowerCase(Locale.ROOT);
        if (registeredSubCommands.containsKey(commandName)) {
            plugin.getLogger().warning("The command " + commandName + " is already registered.");
            return;
        }

        registeredSubCommands.put(commandName, subCommand);
    }

    public List<BaseCommand> getRegisteredCommands() {
        return new ArrayList<>(registeredSubCommands.values());
    }

    public BaseCommand getSubCommand(String name) {
        for (BaseCommand subCommand : registeredSubCommands.values()) {
            if (subCommand.getName().equalsIgnoreCase(name)) {
                return subCommand;
            }
            if (subCommand.getAlias().contains(name.toLowerCase(Locale.ROOT))) {
                return subCommand;
            }
        }

        return null;
    }

    public Commands(String parent) {
        parent = parent.toLowerCase(Locale.ROOT);
        PluginCommand pluginCommand = Objects.requireNonNull(plugin.getCommand(parent), "The command " + parent + " is not written in plugin.yml");
        pluginCommand.setExecutor(this);
        pluginCommand.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player && config.getDisabledWorlds().contains(((Player) sender).getWorld().getName())) {
            messages.sendDisabledWorld(sender);
            return false;
        }
        
        BaseCommand subCommand;
        if (args.length == 0 || (subCommand = getSubCommand(args[0])) == null) {
            BaseCommand helpCommand = getSubCommand("help");
            if (helpCommand == null) {
                messages.sendNotEnoughArguments(sender);
                return false;
            }
            return helpCommand.runCommand(sender, args);
        }

        if (subCommand.isPlayerOnly() && !(sender instanceof Player)) {
            messages.sendPlayerOnly(sender);
            return false;
        }

        if (!subCommand.hasPermission(sender)) {
            messages.sendNoPermission(sender, subCommand.getPermissionNode());
            return false;
        }

        if (!subCommand.isValidArgsLength(args.length)) {
            messages.sendNotEnoughArguments(sender);
            messages.sendUsage(sender, subCommand.getUsage());
            return false;
        }

        return subCommand.runCommand(sender, args);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> permittedCommands = getPermittedCommandNames(sender);
        if (args.length == 1) {
            return StringUtil.copyPartialMatches(args[0], permittedCommands, new ArrayList<>());
        }

        if (!permittedCommands.contains(args[0].toLowerCase(Locale.ROOT))) {
            return List.of();
        }

        return getSubCommand(args[0]).runTabComplete(sender, args);
    }

    private List<String> getPermittedCommandNames(CommandSender sender) {
        List<String> result = new ArrayList<>();
        for (BaseCommand subCommand : registeredSubCommands.values()) {
            if (subCommand.hasPermission(sender)) {
                result.add(subCommand.getName().toLowerCase(Locale.ROOT));
                result.addAll(subCommand.getAlias());
            }
        }
        return result;
    }
}