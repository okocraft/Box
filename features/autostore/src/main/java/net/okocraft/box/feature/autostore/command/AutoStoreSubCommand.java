package net.okocraft.box.feature.autostore.command;

import java.util.List;
import java.util.Locale;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public abstract class AutoStoreSubCommand {

    private final String name;

    public AutoStoreSubCommand(@NotNull String name) {
        this.name = name.toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return this.name;
    }

    abstract void runCommand(CommandSender sender, String[] args, AutoStoreSetting setting);
    abstract List<String> runTabComplete(CommandSender sender, String[] args);
}
