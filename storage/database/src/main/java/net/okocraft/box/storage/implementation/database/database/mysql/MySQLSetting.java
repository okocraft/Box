package net.okocraft.box.storage.implementation.database.database.mysql;

import com.github.siroshun09.configapi.core.serialization.annotation.DefaultInt;
import com.github.siroshun09.configapi.core.serialization.annotation.DefaultString;

public record MySQLSetting(
    @DefaultString("box_") String tablePrefix,
    @DefaultString("address") String address,
    @DefaultInt(3306) int port,
    @DefaultString("box") String databaseName,
    String username,
    String password) {
}
