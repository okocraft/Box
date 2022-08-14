package net.okocraft.box.feature.category.internal.category;

import org.bukkit.Material;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public interface DefaultCategory {

    @Contract("_, _ -> new")
    static @NotNull DefaultCategory create(@NotNull String name, @NotNull Material icon) {
        return new DefaultCategoryRecord(name, icon);
    }

    @NotNull String getName();

    @NotNull Material getIconMaterial();

    record DefaultCategoryRecord(@NotNull String name, @NotNull Material icon) implements DefaultCategory {

        @Override
        public @NotNull String getName() {
            return name;
        }

        @Override
        public @NotNull Material getIconMaterial() {
            return icon;
        }
    }
}
