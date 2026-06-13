package net.okocraft.box.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

record DefaultItemProviderImpl() implements DefaultItemProvider {

    @Override
    public @NotNull Stream<DefaultItem> provide() {
        return new ItemSources.Merger()
            .append(ItemSources.itemTypes())
            .append(ItemSources.potions())
            .append(ItemSources.enchantedBooks())
            .append(ItemSources.fireworks())
            .append(ItemSources.goatHorns())
            .append(ItemSources.ominousBottles())
            .result();
    }

    @Override
    public @NotNull Map<String, String> renamedItems(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
        return RenamedItems.loadFromResource(startingVersion, currentVersion);
    }

    @Override
    public @NotNull UnaryOperator<String> itemNameConvertor(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion) {
        Map<String, String> renameMap = RenamedItems.loadFromResource(startingVersion, currentVersion);
        return name -> renameMap.getOrDefault(name, name);
    }
}
