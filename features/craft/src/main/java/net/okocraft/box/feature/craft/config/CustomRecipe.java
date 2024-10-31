package net.okocraft.box.feature.craft.config;

import dev.siroshun.configapi.core.serialization.annotation.CollectionType;
import dev.siroshun.configapi.core.serialization.annotation.DefaultInt;

import java.util.List;

public record CustomRecipe(
    @CollectionType(String.class) List<String> ingredients,
    String result,
    @DefaultInt(1) int amount) {
}
