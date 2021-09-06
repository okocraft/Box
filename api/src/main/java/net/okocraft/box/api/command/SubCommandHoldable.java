package net.okocraft.box.api.command;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * An interface that can hold subcommands.
 */
public interface SubCommandHoldable {

    /**
     * Gets the {@link SubCommandHolder}.
     *
     * @return the {@link SubCommandHolder}
     */
    @NotNull SubCommandHolder getSubCommandHolder();

    /**
     * A class that holds subcommands.
     */
    class SubCommandHolder {

        private final List<Command> subCommands;

        /**
         * The constructor of {@link SubCommandHolder}.
         *
         * @param subCommands the set of subcommands
         */
        public SubCommandHolder(@NotNull Command... subCommands) {
            this.subCommands = new ArrayList<>(Arrays.asList(subCommands));
        }

        /**
         * Gets the set of subcommands.
         * <p>
         * The returned list can be changed in implementation,
         * but it should not add or remove.
         *
         * @return the set of subcommands
         */
        public @NotNull @Unmodifiable List<Command> getSubCommands() {
            return subCommands;
        }

        /**
         * Registers the new subcommand.
         *
         * @param subCommand the new subcommand
         */
        public void register(@NotNull Command subCommand) {
            subCommands.add(subCommand);
        }

        /**
         * Unregisters the subcommand.
         *
         * @param subCommand the subcommand to unregister
         */
        public void unregister(@NotNull Command subCommand) {
            subCommands.remove(subCommand);
        }

        /**
         * Searches for a command that matches the name or aliases.
         *
         * @param name the name or the alias of the command
         * @return the search result
         */
        public @NotNull Optional<Command> search(@NotNull String name) {
            name = name.toLowerCase(Locale.ROOT);

            for (var subCommand : subCommands) {
                if (subCommand.getName().equals(name) || subCommand.getAliases().contains(name)) {
                    return Optional.of(subCommand);
                }
            }

            return Optional.empty();
        }

        /**
         * Searches for commands that start with a name as an argument, and returns them.
         *
         * @param str the string to search
         * @return the search result
         */
        public @NotNull List<Command> matches(@NotNull String str) {
            var lower = str.toLowerCase(Locale.ROOT);

            return subCommands.stream()
                    .filter(cmd -> cmd.getName().startsWith(lower))
                    .toList();
        }
    }
}
