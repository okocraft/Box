package net.okocraft.box.core.model.manager.event;

import com.github.siroshun09.event4j.caller.AsyncEventCaller;
import com.github.siroshun09.event4j.listener.ListenerBase;
import com.github.siroshun09.event4j.listener.SubscribedListener;
import com.github.siroshun09.event4j.priority.Priority;
import com.github.siroshun09.event4j.simple.EventServiceProvider;
import com.github.siroshun09.event4j.subscriber.EventSubscriber;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.api.model.manager.EventManager;
import net.okocraft.box.api.scheduler.BoxScheduler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.function.Consumer;

public class BoxEventManager implements EventManager {

    private final EventServiceProvider<Key, BoxEvent, Priority> provider;
    private final AsyncEventCaller<BoxEvent> asyncEventCaller;

    public BoxEventManager(@NotNull EventServiceProvider<Key, BoxEvent, Priority> provider, @NotNull BoxScheduler scheduler) {
        this.provider = provider;
        this.asyncEventCaller = AsyncEventCaller.create(provider.caller(), scheduler::runAsyncTask);
    }

    @Override
    public void call(@NotNull BoxEvent event) {
        this.provider.caller().call(event);
    }

    @Override
    public void callAsync(@NotNull BoxEvent event) {
        this.asyncEventCaller.callAsync(event);
    }

    @Override
    public <E extends BoxEvent> void callAsync(@NotNull E event, @Nullable Consumer<? super E> callback) {
        this.asyncEventCaller.callAsync(event, callback);
    }

    @Override
    public @NotNull <E extends BoxEvent> EventSubscriber<Key, E, Priority> getSubscriber(@NotNull Class<E> eventClass) {
        return this.provider.subscriber(eventClass);
    }

    @Override
    public @NotNull Collection<SubscribedListener<Key, ? extends BoxEvent, Priority>> subscribeAll(@NotNull Iterable<ListenerBase<Key, ? extends BoxEvent, Priority>> listeners) {
        return this.provider.subscribeAll(listeners);
    }

    @Override
    public void unsubscribeAll(@NotNull Collection<? extends SubscribedListener<Key, ? extends BoxEvent, Priority>> listeners) {
        this.provider.unsubscribeAll(listeners);
    }

    @Override
    public void unsubscribeByKey(@NotNull Key key) {
        this.provider.unsubscribeByKey(key);
    }
}
