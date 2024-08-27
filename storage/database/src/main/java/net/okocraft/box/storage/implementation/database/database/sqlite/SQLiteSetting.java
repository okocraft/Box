package net.okocraft.box.storage.implementation.database.database.sqlite;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultString;

public record SQLiteSetting(
    @DefaultString("box_") String tablePrefix,
    @DefaultString("box-sqlite.db") String filename
) {
}
