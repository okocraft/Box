package net.okocraft.box.test.shared.storage.memory;

import dev.siroshun.configapi.core.serialization.annotation.Comment;
import dev.siroshun.configapi.core.serialization.annotation.DefaultBoolean;
import dev.siroshun.configapi.core.serialization.annotation.DefaultInt;

// Note: When this record is changed, ConfigTest#EXPECTED_DEFAULT_CONFIG must also be updated.
public record MemoryStorageSetting(
    @Comment("Whether to enable partial saving.")
    @DefaultBoolean(true) boolean partialSaving,
    @Comment(value = "An example setting to test generating default config.", type = "inline")
    @DefaultInt(10) int exampleValue
) {
}
