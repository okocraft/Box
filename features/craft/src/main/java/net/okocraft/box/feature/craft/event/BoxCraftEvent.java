package net.okocraft.box.feature.craft.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.Cancellable;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * An event that called when the {@link Player} is crafting items in Box.
 */
public class BoxCraftEvent extends BoxEvent implements Cancellable {

    private final Player crafter;
    private final SelectedRecipe selectedRecipe;
    private final int times;
    private boolean cancelled;

    /**
     * The constructor of {@link BoxCraftEvent}.
     *
     * @param crafter        the {@link Player} who craft items
     * @param selectedRecipe the {@link SelectedRecipe} used to craft items
     * @param times          the number of times crafted
     */
    public BoxCraftEvent(@NotNull Player crafter, @NotNull SelectedRecipe selectedRecipe, int times) {
        this.crafter = Objects.requireNonNull(crafter);
        this.selectedRecipe = Objects.requireNonNull(selectedRecipe);
        this.times = times;
    }

    /**
     * Gets the player who is trying to craft.
     *
     * @return the player who is trying to craft
     */
    public @NotNull Player getCrafter() {
        return this.crafter;
    }

    /**
     * Gets the recipe to craft.
     *
     * @return the recipe to craft
     */
    public @NotNull SelectedRecipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    /**
     * Gets the number of times to craft.
     *
     * @return the number of times to craft
     */
    public int getTimes() {
        return this.times;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public String toString() {
        return "BoxCraftEvent{" +
            "crafter=" + this.crafter +
            ", selectedRecipe=" + this.selectedRecipe +
            ", times=" + this.times +
            ", cancelled=" + this.cancelled +
            '}';
    }

    @Override
    public @NotNull String toDebugLog() {
        return "BoxCraftEvent{" +
            "crafterUuid=" + this.crafter.getUniqueId() +
            ", crafterName=" + this.crafter.getName() +
            ", selectedRecipe=" + this.selectedRecipe +
            ", times=" + this.times +
            '}';
    }
}
