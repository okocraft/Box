package net.okocraft.box.api.command;

import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * An abstract class for a sub command of /box or /boxadmin.
 * <p>
 * {@code name} is used as in /box &lt;name&gt;.
 * <p>
 * The permission node is for executing that command,
 * and it will check the executor before {@link #onCommand(CommandSender, String[])} is called.
 */
public abstract class AbstractCommand implements Command {

    private final String name;
    private final String permissionNode;
    private final Set<String> aliases;

    /**
     * The constructor of {@link AbstractCommand}.
     *
     * @param name           the command name
     * @param permissionNode the permission node
     */
    public AbstractCommand(@NotNull String name, @NotNull String permissionNode) {
        this(name, permissionNode, Collections.emptySet());
    }

    /**
     * The constructor of {@link AbstractCommand}.
     *
     * @param name           the command name
     * @param permissionNode the permission node
     * @param aliases        the set of aliases
     */
    public AbstractCommand(@NotNull String name, @NotNull String permissionNode, @NotNull Set<String> aliases) {
        this.name = name;
        this.permissionNode = permissionNode;
        this.aliases = aliases;
    }

    public @NotNull String getName() {
        return name;
    }

    public @NotNull String getPermissionNode() {
        return permissionNode;
    }

    public @NotNull @Unmodifiable Set<String> getAliases() {
        return aliases;
    }

    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
