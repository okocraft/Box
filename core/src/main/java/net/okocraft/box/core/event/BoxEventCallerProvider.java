package net.okocraft.box.core.event;

import dev.siroshun.event4j.api.caller.EventCaller;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.event.caller.EventCallerProvider;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.jetbrains.annotations.NotNull;

public class BoxEventCallerProvider implements EventCallerProvider {

    private final EventCaller<BoxEvent> syncCaller;
    private final EventCaller<BoxEvent> asyncCaller;

    public BoxEventCallerProvider(@NotNull EventCaller<BoxEvent> eventCaller, @NotNull BoxScheduler scheduler) {
        this.syncCaller = eventCaller;
        this.asyncCaller = EventCaller.asyncCaller(eventCaller, scheduler::runAsyncTask);
    }

    @Override
    public @NotNull EventCaller<BoxEvent> sync() {
        return this.syncCaller;
    }

    @Override
    public @NotNull EventCaller<BoxEvent> async() {
        return this.asyncCaller;
    }

}
