package net.okocraft.box.feature.craft.loader;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.serialization.key.KeyGenerator;
import dev.siroshun.configapi.core.serialization.record.RecordDeserializer;
import dev.siroshun.configapi.core.serialization.record.RecordSerializer;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.config.RecipeConfig;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public final class RecipeLoader {

    @SuppressWarnings("UnstableApiUsage")
    public static @NotNull Map<BoxItem, RecipeHolder> load(@NotNull Path filepath) throws IOException {
        var deserializer = RecordDeserializer.create(RecipeConfig.class, KeyGenerator.CAMEL_TO_KEBAB);
        RecipeConfig config;

        if (Files.isRegularFile(filepath)) {
            config = deserializer.deserialize(YamlFormat.DEFAULT.load(filepath));
        } else {
            config = deserializer.deserialize(MapNode.empty()); // generate default config
            YamlFormat.COMMENT_PROCESSING.save(RecordSerializer.create(KeyGenerator.CAMEL_TO_KEBAB).serialize(config), filepath);
        }

        var processor = new Processor(config);
        var iterator = Bukkit.recipeIterator();
        while (iterator.hasNext()) {
            try {
                processor.processRecipe(iterator.next());
            } catch (IllegalArgumentException ignored) {
            }
        }

        AdditionalRecipes.addFireworkRocketRecipes(processor);

        processor.processCustomRecipes();

        return processor.result();
    }
}
