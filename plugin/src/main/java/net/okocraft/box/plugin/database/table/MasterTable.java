package net.okocraft.box.plugin.database.table;

import net.okocraft.box.plugin.database.Storage;
import net.okocraft.box.plugin.database.connector.Database;
import net.okocraft.box.plugin.model.User;
import net.okocraft.box.plugin.model.item.Item;
import net.okocraft.box.plugin.model.item.Stock;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Optional;

public class MasterTable extends AbstractTable {

    private final static String PLAYER_STOCK_SELECT_ALL = "select itemid, stock, autostore from %table% where playerid=?";
    private final static String PLAYER_STOCK_SELECT = "select stock, autostore form %table% where playerid=? and itemid=?";
    private final static String PLAYER_STOCK_INSERT = "insert into %table% (playerid, itemid, stock, autostore) values(?,?,0,0)";
    private final static String PLAYER_STOCK_UPDATE = "update %table% set stock=?, autostore=? where playerid=? and itemid=?";
    private final static Map<Database.Type, String> PLAYER_DEFAULT_STOCK_INSERT_OR_IGNORE = Map.of(
            Database.Type.SQLITE, "insert or ignore into %table% (playerid, itemid, stock, autostore) values(?,?,0,0)",
            Database.Type.MYSQL, "insert ignore into %table% (playerid, itemid, stock, autostore) values(?,?,0,0)");

    private final Storage storage;

    public MasterTable(@NotNull Database database, @NotNull Storage storage, @NotNull String prefix) {
        super(database, prefix + "master");

        this.storage = storage;
    }

    @NotNull
    public User loadUserData(@NotNull User user) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(PLAYER_STOCK_SELECT_ALL))) {
            st.setInt(1, user.getInternalID());

            try (ResultSet result = st.executeQuery()) {
                while (result.next()) {
                    Optional<Item> item = storage.getItemById(result.getInt("itemid"));
                    if (item.isPresent()) {
                        boolean autoStore = result.getInt("autostore") == 1;
                        user.setStock(new Stock(item.get(), result.getInt("stock"), autoStore));
                    }
                }
            }
        }

        return user;
    }

    @NotNull
    public Stock createOrLoadStock(int playerId, @NotNull Item item) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(PLAYER_STOCK_SELECT))) {
            st.setInt(1, playerId);
            st.setInt(2, item.getInternalID());

            try (ResultSet result = st.executeQuery()) {
                if (result.next()) {
                    return new Stock(item, result.getInt("stock"), result.getInt("autostore") == 1);
                }
            }
        }

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(PLAYER_STOCK_INSERT))) {
            st.setInt(1, playerId);
            st.setInt(2, item.getInternalID());

            st.execute();
        }

        return new Stock(item, 0, false);
    }

    public void saveDefaultUserData(@NotNull User user) throws SQLException {
        String sql = PLAYER_DEFAULT_STOCK_INSERT_OR_IGNORE.get(database.getType());
        if (sql == null) {
            throw new IllegalStateException("Could not get SQL.");
        }

        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(sql))) {
            for (Item item : storage.getItems()) {
                st.setInt(1, user.getInternalID());
                st.setInt(2, item.getInternalID());

                st.addBatch();
            }

            st.executeBatch();
        }
    }

    public void saveStock(@NotNull User user, @NotNull Stock stock) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(PLAYER_STOCK_UPDATE))) {
            st.setInt(1, stock.getAmount());
            st.setInt(2, stock.isAutoStore() ? 1 : 0);
            st.setInt(3, user.getInternalID());
            st.setInt(4, stock.getItem().getInternalID());

            st.execute();
        }
    }

    public void saveUserData(@NotNull User user) throws SQLException {
        try (Connection c = database.getConnection();
             PreparedStatement st = c.prepareStatement(replaceTableName(PLAYER_STOCK_UPDATE))) {
            for (Stock stock : user.getStocks()) {
                st.setInt(1, stock.getAmount());
                st.setInt(2, stock.isAutoStore() ? 1 : 0);
                st.setInt(3, user.getInternalID());
                st.setInt(4, stock.getItem().getInternalID());

                st.addBatch();
            }

            st.executeBatch();
        }
    }

    @Override
    protected void createTable() throws SQLException {
        try (Connection c = database.getConnection(); Statement statement = c.createStatement()) {

            statement.addBatch(
                    "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                            "id INTEGER PRIMARY KEY " + getAutoIncrementSQL() + ", " +
                            "playerid INTEGER NOT NULL, " +
                            "itemid INTEGER NOT NULL, " +
                            "stock INTEGER NOT NULL DEFAULT 0, " +
                            "autostore INTEGER NOT NULL DEFAULT 0, " +
                            "UNIQUE(playerid, itemid))"
            );

            statement.executeBatch();
        }
    }
}
