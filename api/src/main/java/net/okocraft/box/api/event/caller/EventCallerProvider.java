package net.okocraft.box.api.event.caller;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import org.jetbrains.annotations.NotNull;

public interface EventCallerProvider {

    @NotNull EventCaller<BoxEvent> sync();

    @NotNull EventCaller<BoxEvent> async();

}
