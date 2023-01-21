package net.okocraft.box.storage.implementation.database.table;

import net.okocraft.box.api.model.stock.StockData;
import net.okocraft.box.api.model.stock.UserStockHolder;
import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.stock.UserStockHolderFactory;
import net.okocraft.box.storage.api.holder.LoggerHolder;
import net.okocraft.box.storage.api.model.stock.StockStorage;
import net.okocraft.box.storage.api.util.item.BoxItemSupplier;
import net.okocraft.box.storage.implementation.database.database.Database;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;

// | uuid | item_id | amount |
public class StockTable extends AbstractTable implements StockStorage {

    public StockTable(@NotNull Database database) {
        super(database, database.getSchemaSet().stockTable());
    }

    @Override
    public void init() throws Exception {
        createTableAndIndex();
    }

    @Override
    public @NotNull UserStockHolder loadUserStockHolder(@NotNull BoxUser user) throws Exception {
        var stock = new ArrayList<StockData>();

        try (var connection = database.getConnection();
             var statement = prepareStatement(connection, "SELECT item_id, amount FROM `%table%` WHERE uuid=?")) {
            var strUuid = user.getUUID().toString();
            statement.setString(1, strUuid);

            try (var resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int itemId = resultSet.getInt("item_id");
                    int amount = resultSet.getInt("amount");

                    var item = BoxItemSupplier.getItem(itemId);

                    if (item.isPresent()) {
                        stock.add(new StockData(item.get(), amount));
                    } else {
                        LoggerHolder.get().warning("Unknown item id: " + itemId + " (" + strUuid + ")");
                    }
                }
            }
        }

        return UserStockHolderFactory.create(user, stock);
    }

    @Override
    public void saveUserStockHolder(@NotNull UserStockHolder stockHolder) throws Exception {
        try (var connection = database.getConnection()) {
            deleteUserData(connection, stockHolder.getUser());
            insertUserData(connection, stockHolder.getUser(), stockHolder.toStockDataCollection());
        }
    }

    private void deleteUserData(@NotNull Connection connection, @NotNull BoxUser user) throws SQLException {
        try (var statement = prepareStatement(connection, "DELETE FROM `%table%` WHERE uuid=?")) {
            statement.setString(1, user.getUUID().toString());
            statement.execute();
        }
    }

    private void insertUserData(@NotNull Connection connection, @NotNull BoxUser user,
                                @NotNull Collection<StockData> stockDataCollection) throws SQLException {
        try (var statement = prepareStatement(connection, "INSERT INTO `%table%` (uuid, item_id, amount) VALUES(?,?,?)")) {
            var strUuid = user.getUUID().toString();

            for (var data : stockDataCollection) {
                if (data.amount() == 0) {
                    continue;
                }

                statement.setString(1, strUuid);
                statement.setInt(2, data.item().getInternalId());
                statement.setInt(3, data.amount());
                statement.addBatch();
            }

            statement.executeBatch();
        }
    }
}
