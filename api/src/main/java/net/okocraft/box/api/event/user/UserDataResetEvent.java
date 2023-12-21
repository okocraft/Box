package net.okocraft.box.api.event.user;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

/**
 * An event called when {@code /boxadmin resetall &lt;username&gt;} is executed.
 */
public class UserDataResetEvent extends BoxEvent {

    private final BoxUser user;

    /**
     * The constructor of a {@link UserDataResetEvent}.
     *
     * @param user the user of this event
     */
    public UserDataResetEvent(@NotNull BoxUser user) {
        this.user = user;
    }

    /**
     * Gets the user being reset
     *
     * @return the user being reset
     */
    public @NotNull BoxUser getUser() {
        return user;
    }

    @Override
    public @NotNull String toDebugLog() {
        return "UserDataResetEvent{" +
                "uuid=" + user.getUUID() +
                ", name=" + user.getName() +
                '}';
    }

    @Override
    public String toString() {
        return "UserDataResetEvent{" +
                "user=" + user +
                '}';
    }
}
