package net.okocraft.box.core.config;

import com.github.siroshun09.configapi.core.serialization.annotation.CollectionType;
import com.github.siroshun09.configapi.core.serialization.annotation.Comment;

import java.util.Set;

// Note: When this record is changed, ConfigTest#EXPECTED_DEFAULT_CONFIG must also be updated.
public record CoreSetting(
        @Comment("The list of worlds where Box cannot be used.")
        @CollectionType(String.class) Set<String> disabledWorlds,
        @Comment("Whether to enable debug mode or not.")
        boolean debug
) {
}
