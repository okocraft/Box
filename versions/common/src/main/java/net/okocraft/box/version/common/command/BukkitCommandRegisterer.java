package net.okocraft.box.version.common.command;

import net.okocraft.box.api.command.Command;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class BukkitCommandRegisterer {

    public static void register(@NotNull String fallbackPrefix, @NotNull Command command) {
        Bukkit.getServer().getCommandMap().register(fallbackPrefix, new BukkitCommandImpl(command));
    }

    private static class BukkitCommandImpl extends org.bukkit.command.Command {

        private final Command command;

        private BukkitCommandImpl(@NotNull Command command) {
            super(command.getName(), "", "", List.copyOf(command.getAliases()));
            this.command = command;
        }


        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            this.command.onCommand(sender, args);
            return true;
        }

        @Override
        public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
            return this.command.onTabComplete(sender, args);
        }
    }

    private BukkitCommandRegisterer() {
    }
}
