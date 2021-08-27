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
public abstract class AbstractCommand {

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

    /**
     * Gets the command name.
     *
     * @return the command name
     */
    public @NotNull String getName() {
        return name;
    }

    /**
     * Gets the permission node.
     *
     * @return the permission node
     */
    public @NotNull String getPermissionNode() {
        return permissionNode;
    }

    /**
     * Gets the set of aliases.
     *
     * @return the set of aliases
     */
    public @NotNull @Unmodifiable Set<String> getAliases() {
        return aliases;
    }

    /**
     * Executes the command.
     * <p>
     * When this method is called, the executor has the permission
     * and the length of the argument array is greater than or equal to 1.
     *
     * @param sender the executor
     * @param args   the array of arguments
     */
    public abstract void onCommand(@NotNull CommandSender sender, @NotNull String[] args);

    /**
     * Gets the tab-completion.
     * <p>
     * When this method is called, the executor has the permission
     * and the length of the argument array is greater than or equal to 1.
     *
     * @param sender the executor
     * @param args   the array of arguments
     * @return the result of the tab-completion or an empty list
     */
    public @NotNull List<String> onTabComplete(@NotNull CommandSender sender, @NotNull String[] args) {
        return Collections.emptyList();
    }
}
