package net.okocraft.box.feature.craft.config;

import dev.siroshun.serialization.annotation.CollectionType;
import dev.siroshun.serialization.annotation.Comment;

import java.util.List;
import java.util.Set;

public record RecipeConfig(
    @Comment("""
        The list to disable default recipes.
        
        Example setting:
        
        disabled-recipes:
          - "minecraft:crossbow"
        """)
    @CollectionType(String.class) Set<String> disabledRecipes,

    @Comment("""
        The list to create custom recipes in Box
        
        custom-recipes:
          - ingredients: # the name of the item to be used as an ingredient. (1~9 items, "air" will be empty slot)
              - WHITE_CONCRETE_POWDER
            result: WHITE_CONCRETE # result item name (in Box)
            amount: 1 # the amount of the result item
        """)
    @CollectionType(CustomRecipe.class) List<CustomRecipe> customRecipes
) {
}
