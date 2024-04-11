package net.okocraft.box.core.model.manager.event;

import net.okocraft.box.api.event.BoxEvent;
import net.okocraft.box.test.shared.scheduler.ScheduledTask;
import net.okocraft.box.test.shared.scheduler.TestScheduler;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

class BoxEventManagerTest {

    @Test
    void testInitializeAsyncCaller() {
        var manager = BoxEventManager.create();
        var event = new BoxEvent();

        { // Checks if the EventManager#callAsync can be called without BoxScheduler
            var future = new CompletableFuture<BoxEvent>();
            manager.callAsync(event, future::complete);
            Assertions.assertSame(event, future.join());
        }

        { // Checks if it calls an event async by BoxScheduler
            var scheduler = new TestScheduler(true);
            manager.initializeAsyncEventCaller(scheduler);

            var future = new CompletableFuture<BoxEvent>();
            manager.callAsync(event, future::complete);
            scheduler.checkTask(task -> {
                Assertions.assertEquals(task.delay(), Duration.ofSeconds(0));
                Assertions.assertEquals(task.interval(), Duration.ofSeconds(0));
                Assertions.assertEquals(task.type(), ScheduledTask.Type.ASYNC);
            });
            Assertions.assertSame(event, future.join());
        }
    }

    // Other methods are just delegating to EventServiceProvider.
}
