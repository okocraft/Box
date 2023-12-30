package net.okocraft.box.feature.gui.api.button;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.feature.gui.api.menu.Menu;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public sealed interface ClickResult permits ClickResult.ChangeMenu, ClickResult.WaitingTask, SimpleClickResult {

    ClickResult UPDATE_ICONS = new SimpleClickResult("update_menu");
    ClickResult UPDATE_BUTTON = new SimpleClickResult("update_button");
    ClickResult NO_UPDATE_NEEDED = new SimpleClickResult("no_update_needed");
    ClickResult BACK_MENU = new SimpleClickResult("back_menu");

    static @NotNull ClickResult changeMenu(@NotNull Menu menu) {
        return new ChangeMenu(menu);
    }

    static @NotNull WaitingTask waitingTask() {
        return new WaitingTask();
    }

    record ChangeMenu(@NotNull Menu menu) implements ClickResult {
    }

    final class WaitingTask implements ClickResult {

        private ClickResult result;
        private Consumer<ClickResult> consumer;

        public void onCompleted(@NotNull Consumer<ClickResult> consumer) {

            synchronized (this) {
                if (this.result == null) {
                    this.consumer = consumer;
                } else {
                    consumer.accept(this.result);
                }
            }
        }

        public void complete(@NotNull ClickResult result) {
            synchronized (this) {
                if (this.consumer == null) {
                    this.result = result;
                } else {
                    this.consumer.accept(result);
                }
            }
        }

        public void completeAsync(@NotNull ClickResult result) {
            synchronized (this) {
                if (this.consumer == null) {
                    this.result = result;
                } else {
                    BoxAPI.api().getScheduler().runAsyncTask(() -> this.consumer.accept(result));
                }
            }
        }
    }
}
