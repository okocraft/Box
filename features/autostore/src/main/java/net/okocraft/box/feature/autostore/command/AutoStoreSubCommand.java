package net.okocraft.box.feature.autostore.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;

public abstract class AutoStoreSubCommand {

    private final String name;
    private final Set<String> aliases;

    public AutoStoreSubCommand(@NotNull String name) {
        this.name = name.toLowerCase(Locale.ROOT);
        this.aliases = createAliases(name.toLowerCase(Locale.ROOT));
    }

    public String getName() {
        return this.name;
    }

    @UnmodifiableView
    public Set<String> getAliases() {
        return Collections.unmodifiableSet(this.aliases);
    }

    public boolean isNameOrAlias(String nameOrAlias) {
        if (name.equalsIgnoreCase(nameOrAlias)) {
            return true;
        }

        for (String alias : aliases) {
            if (alias.equalsIgnoreCase(nameOrAlias)) {
                return true;
            }
        }

        return false;
    }

    abstract void runCommand(CommandSender sender, String[] args, AutoStoreSetting setting);
    abstract List<String> runTabComplete(CommandSender sender, String[] args);

    private static Set<String> createAliases(String from) {
        List<String> aliases = new ArrayList<>();
        for (char c : from.toCharArray()) {
            String aliasPart = String.valueOf(c);
            if (aliases.size() == 0) {
                aliases.add(aliasPart);
            } else {
                aliases.add(aliases.get(aliases.size() - 1) + aliasPart);
            }
        }
        return new HashSet<>(aliases);
    }
}
