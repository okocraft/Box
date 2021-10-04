package net.okocraft.box.core.util.executor;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.util.ExecutorProvider;
import net.okocraft.box.core.taskfactory.BoxTaskFactory;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executor;

public class BoxExecutorProvider implements ExecutorProvider {

    @Override
    public @NotNull Executor getExecutor() {
        return ((BoxTaskFactory) BoxProvider.get().getTaskFactory()).getExecutor();
    }

    @Override
    public @NotNull Executor getMainThread() {
        return ((BoxTaskFactory) BoxProvider.get().getTaskFactory()).getMainThread();
    }
}
