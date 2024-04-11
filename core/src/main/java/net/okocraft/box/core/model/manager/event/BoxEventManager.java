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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class BoxEventManager implements EventManager {

    @Contract(" -> new")
    public static @NotNull BoxEventManager create() {
        return new BoxEventManager(
                EventServiceProvider.factory()
                        .keyClass(Key.class)
                        .eventClass(BoxEvent.class)
                        .orderComparator(Priority.COMPARATOR, Priority.NORMAL)
                        .create()
        );
    }

    private final EventServiceProvider<Key, BoxEvent, Priority> provider;
    private @Nullable AsyncEventCaller<BoxEvent> asyncEventCaller; // initialize later

    private BoxEventManager(@NotNull EventServiceProvider<Key, BoxEvent, Priority> provider) {
        this.provider = provider;
    }

    @Override
    public void call(@NotNull BoxEvent event) {
        this.provider.caller().call(event);
    }

    @Override
    public void callAsync(@NotNull BoxEvent event) {
        this.callAsync(event, null);
    }

    @SuppressWarnings("resource")
    @Override
    public <E extends BoxEvent> void callAsync(@NotNull E event, @Nullable Consumer<? super E> callback) {
        if (this.asyncEventCaller != null) {
            this.asyncEventCaller.callAsync(event, callback);
        } else {
            ForkJoinPool.commonPool().execute(() -> {
                this.call(event);
                if (callback != null) {
                    callback.accept(event);
                }
            });
        }
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

    public void initializeAsyncEventCaller(@NotNull BoxScheduler scheduler) {
        this.asyncEventCaller = AsyncEventCaller.create(this.provider.caller(), scheduler::runAsyncTask);
    }
}
