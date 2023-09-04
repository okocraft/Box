package net.okocraft.box.core.model.stock;

import net.okocraft.box.api.model.user.BoxUser;
import org.jetbrains.annotations.NotNull;

interface NameHolder {

    @NotNull String get();

    record Value(@NotNull String name) implements NameHolder {
        @Override
        public @NotNull String get() {
            return name;
        }
    }

    record FromBoxUser(@NotNull BoxUser user) implements NameHolder {
        @Override
        public @NotNull String get() {
            return user.getName().orElse("Unknown");
        }
    }
}
