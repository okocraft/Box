package net.okocraft.box.feature.craft.event;

import com.github.siroshun09.event4j.event.Cancellable;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.feature.craft.model.SelectedRecipe;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BoxCraftEvent extends BoxEvent implements Cancellable {

    private final Player crafter;
    private final SelectedRecipe selectedRecipe;
    private final int times;
    private boolean cancelled;

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
        return crafter;
    }

    /**
     * Gets the recipe to craft.
     *
     * @return the recipe to craft
     */
    public @NotNull SelectedRecipe getSelectedRecipe() {
        return selectedRecipe;
    }

    /**
     * Gets the number of times to craft.
     *
     * @return the number of times to craft
     */
    public int getTimes() {
        return times;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    @Override
    public String toString() {
        return "BoxCraftEvent{" +
                "crafter=" + crafter +
                ", selectedRecipe=" + selectedRecipe +
                ", times=" + times +
                ", cancelled=" + cancelled +
                '}';
    }

    @Override
    public @NotNull String toDebugLog() {
        return "BoxCraftEvent{" +
                "crafterUuid=" + crafter.getUniqueId() +
                ", crafterName=" + crafter.getName() +
                ", selectedRecipe=" + selectedRecipe +
                ", times=" + times +
                '}';
    }
}
