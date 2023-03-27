package net.okocraft.box.core.event;

import com.github.siroshun09.event4j.bus.EventBus;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.core.util.executor.ExecutorProvider;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class EventBusHolder {

    private static final String EVENTBUS_EXECUTOR_NAME = "Event";

    @Contract("_ -> new")
    public static @NotNull EventBusHolder initialize(@NotNull ExecutorProvider provider) {
        return new EventBusHolder(provider);
    }

    private final ExecutorProvider provider;
    private final EventBus<BoxEvent> eventBus;

    private EventBusHolder(@NotNull ExecutorProvider provider) {
        this.provider = provider;
        this.eventBus = EventBus.create(BoxEvent.class, provider.newSingleThreadExecutor(EVENTBUS_EXECUTOR_NAME));
    }

    public @NotNull EventBus<BoxEvent> getEventBus() {
        return eventBus;
    }

    public void close() {
        eventBus.close();
        provider.shutdownExecutor(EVENTBUS_EXECUTOR_NAME);
    }
}
