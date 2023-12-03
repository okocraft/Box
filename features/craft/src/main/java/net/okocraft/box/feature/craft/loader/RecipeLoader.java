package net.okocraft.box.feature.craft.loader;

import com.github.siroshun09.configapi.core.node.MapNode;
import com.github.siroshun09.configapi.core.serialization.key.KeyGenerator;
import com.github.siroshun09.configapi.core.serialization.record.RecordDeserializer;
import com.github.siroshun09.configapi.core.serialization.record.RecordSerializer;
import com.github.siroshun09.configapi.format.yaml.YamlFormat;
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

        Bukkit.recipeIterator().forEachRemaining(processor::processRecipe);

        AdditionalRecipes.getFireworkRocketRecipes().forEach(processor::processRecipe);

        processor.processCustomRecipes();

        return processor.result();
    }
}
