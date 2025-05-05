package net.okocraft.box.core.config;

import dev.siroshun.serialization.annotation.CollectionType;
import dev.siroshun.serialization.annotation.Comment;

import java.util.Set;

// Note: When this record is changed, ConfigTest#EXPECTED_DEFAULT_CONFIG must also be updated.
public record CoreSetting(
    @Comment("Settings related to player stock data.")
    StockDataSetting stockData,
    @Comment("The list of worlds where Box cannot be used.")
    @CollectionType(String.class) Set<String> disabledWorlds,
    @Comment("Whether to enable debug mode or not.")
    boolean debug
) {
}
