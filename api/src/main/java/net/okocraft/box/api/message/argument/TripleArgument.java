package net.okocraft.box.api.message.argument;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

/**
 * An interface to create a component with three arguments.
 *
 * @param <A1> first argument type
 * @param <A2> second argument type
 * @param <A3> third argument type
 */
public interface TripleArgument<A1, A2, A3> {
    /**
     * Creates a {@link Component}.
     *
     * @param a1 first argument
     * @param a2 second argument
     * @param a3 third argument
     * @return the {@link Component} that is applied arguments
     */
    @NotNull Component apply(@NotNull A1 a1, @NotNull A2 a2, @NotNull A3 a3);
}
