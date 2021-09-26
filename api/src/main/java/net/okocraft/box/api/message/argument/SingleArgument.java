package net.okocraft.box.api.message.argument;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to create a component with one argument.
 *
 * @param <A1> the argument type
 */
public interface SingleArgument<A1> {

    /**
     * Creates a {@link Component}.
     *
     * @param a1 the argument
     * @return the {@link Component} that is applied the argument
     */
    @NotNull Component apply(@NotNull A1 a1);

}
