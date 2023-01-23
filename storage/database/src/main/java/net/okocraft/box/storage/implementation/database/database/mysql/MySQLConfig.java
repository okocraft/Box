package net.okocraft.box.storage.implementation.database.database.mysql;

import com.github.siroshun09.configapi.api.Configuration;
import org.jetbrains.annotations.NotNull;

public class MySQLConfig {

    final String tablePrefix;
    final String address;
    final int port;
    final String databaseName;
    final String username;
    final String password;

    public MySQLConfig(@NotNull Configuration config) {
        this.tablePrefix =  config.getString("table-prefix");
        this.address =  config.getString("address");
        this.port = config.getInteger("port");
        this.databaseName = config.getString("database-name");
        this.username = config.getString("username");
        this.password = config.getString("password");
    }
}
