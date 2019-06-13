package net.okocraft.box.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import net.okocraft.box.Box;
import net.okocraft.box.command.Commands;

// NOTE: メッセージがハードコーディングされているが、システム側メッセージなのでとりあえず無視する。
public class Database {
    /**
     * データベルファイルへの URL 。{@code plugins/Box/data.db}
     */
    @Getter
    private String fileUrl;

    /**
     * データベースへの URL 。{@code jdbc:sqlite:database}
     */
    @Getter
    private String DBUrl;

    /**
     * データベース接続のプロパティ
     */
    private final Properties DBProps;

    /**
     * データベースの参照用スレッドプール
     */
    private final ExecutorService threadPool;

    /**
     * データベースへの接続。
     */
    private static Optional<Connection> connection = Optional.empty();

    /**
     * ロガー
     */
    private static Logger log;

    /**
     * テーブル名
     */
    private static String table = "Box";

    public Database(Plugin plugin) {
        // Configure database properties
        DBProps = new Properties();
        DBProps.put("journal_mode", "WAL");
        DBProps.put("synchronous", "NORMAL");

        // Create new thread pool
        threadPool = Executors.newSingleThreadExecutor();

        log = plugin.getLogger();
    }

    /**
     * データベースの初期化を行う。
     *
     * <p>
     * データベースのファイル自体が存在しない場合はファイルを作成する。
     * ファイル内になんらデータベースが存在しない場合、データベースを新たに生成する。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     */
    public boolean connect(String url) {
        // Check if driver exists
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            log.severe("There's no JDBC driver.");
            e.printStackTrace();

            return false;
        }

        // Set DB URL
        fileUrl = url;
        DBUrl = "jdbc:sqlite:" + url;

        // Check if the database file exists.
        // If not exist, attempt to create the file.
        try {
            val file = Paths.get(fileUrl);

            if (!Files.exists(file)) {
                Files.createFile(file);
            }
        } catch (IOException e) {
            // log.error("Failed to create database file.");
            log.severe("Failed to create database file.");
            e.printStackTrace();

            return false;
        }

        // Connect to database
        connection = getConnection(DBUrl, DBProps);

        if (!connection.isPresent()) {
            // log.error("Failed to connect the database.");
            log.severe("Failed to connect the database.");

            return false;
        }

        // create table for Box plugin
        val statement = prepare(
                "CREATE TABLE IF NOT EXISTS " + table + " (uuid TEXT PRIMARY KEY NOT NULL, player TEXT NOT NULL)"
        );

        boolean isTableCreated = statement.map(resource -> {
            try (PreparedStatement stmt = resource) {
                stmt.execute();

                return true;
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        }).orElse(false);

        if (!isTableCreated) {
            log.severe("Failed to create the table.");

            return false;
        }

        val allItems = Box.getInstance().getConfigManager().getAllItems();

        allItems.forEach(itemName -> {
            addColumn(itemName, "INTEGER", "0", false);
            addColumn("autostore_" + itemName, "TEXT", "false", false);
        });

        return true;
    }

    /**
     * データベースへの接続を切断する。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     */
    public void dispose() {
        connection.ifPresent(connection -> {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        connection = Optional.empty();
    }

    /**
     * コネクションをリセットする。
     */
    public void resetConnection() {
        log.info("Disconnecting.");

        dispose();

        log.info("Getting connection.");

        if (!connect(fileUrl)) {
            log.info("Failed to reset connection. Disabling Box plugin.");
            Bukkit.getPluginManager().disablePlugin(Box.getInstance());
        }

        log.info("Database reset complete.");
    }

    /**
     * データベースにレコードを追加する。showWarning が true で失敗した場合はコンソールにログを出力する。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     *
     * @param uuid        UUID
     * @param name        名前
     * @param showWarning コンソールログを出力するかどうか
     *
     */
    public void addPlayer(@NonNull String uuid, @NonNull String name, boolean showWarning) {
        if (existPlayer(uuid)) {
            if (showWarning) {
                log.warning(":PLAYER_" + name + "_UUID_" + uuid + "ALREADY_EXIST");
            }

            return;
        }

        if (Commands.checkEntryType(uuid).equals("player")) {
            if (showWarning) {
                log.warning(":INVALID_UUID");
            }

            return;
        }

        if (!name.matches("(\\d|[a-zA-Z]|_){3,16}")) {
            if (showWarning) {
                log.warning(":INVALID_NAME");
            }

            return;
        }

        prepare("INSERT OR IGNORE INTO " + table + " (uuid, player) VALUES (?, ?)").ifPresent(statement -> {
            try {
                statement.setString(1, uuid);
                statement.setString(2, name);
                statement.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * テーブルからレコードを削除する。 失敗した場合はコンソールにログを出力する。
     *
     * @since 1.1.0-SNAPSHOT
     * @author LazyGon
     *
     * @param entry プレイヤー
     *
     */
    public void removePlayer(@NonNull String entry) {
        if (!existPlayer(entry)) {
            log.warning(":NO_RECORD_FOR_" + entry + "_EXIST");
            return;
        }

        String entryType = Commands.checkEntryType(entry);

        prepare("DELETE FROM " + table + " WHERE " + entryType + " = ?").ifPresent(statement -> {
            try {
                statement.setString(1, entry);
                statement.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * テーブルのデータベースに名前が記録されているか調べる。
     *
     * @since 1.0.0-SNAPSHOT
     * @author LazyGon
     *
     * @param entry uuidでもmcidでも可
     */
    public boolean existPlayer(@NonNull String entry) {
        val playersMap = getPlayersMap();

        return playersMap.containsKey(entry) || playersMap.containsValue(entry);
    }

    /**
     * {@code table}の{@code column}に値をセットする。
     *
     * @since 1.0.0-SNAPSHOT
     * @author LazyGon
     *@param column 更新する列
     * @param entry  プレイヤー。uuidでもmcidでも可
     * @param value  新しい値
     */
    public void set(@NonNull String column, @NonNull String entry, String value) {
        if (!getColumnMap().keySet().contains(column)) {
            log.warning(":NO_COLUMN_NAMED_" + column + "_EXIST");
            return;
        }

        if (!existPlayer(entry)) {
            log.warning(":NO_RECORD_FOR_" + entry + "_EXIST");
            return;
        }

        String entryType = Commands.checkEntryType(entry);

        prepare("UPDATE " + table + " SET " + column + " = ? WHERE " + entryType + " = ?").ifPresent(statement -> {
            try {
                statement.setString(1, value);
                statement.setString(2, entry);
                statement.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * {@code table} で指定したテーブルの列 {@code column} の値を取得する。
     * テーブル、カラム、レコードのいずれかが存在しない場合は対応するエラー文字列を返す。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param column 列
     * @param entry  エントリ
     *
     * @return 値
     */
    public String get(String column, String entry) {
        if (!getColumnMap().keySet().contains(column)) {
            return ":NO_COLUMN_NAMED_" + column + "_EXIST";
        }

        if (!existPlayer(entry)) {
            return ":NO_RECORD_FOR_" + entry + "_EXIST";
        }

        val entryType = Commands.checkEntryType(entry);
        val statement = prepare("SELECT " + column + " FROM " + table + " WHERE " + entryType + " = ?");

        return statement.map(resource -> {
            try (val stmt = resource) {
                stmt.setString(1, entry);

                val result = stmt.executeQuery();

                return result.getString(column);
            } catch (SQLException e) {
                e.printStackTrace();

                return "";
            }
        }).orElse(":NOTHING");
    }

    /**
     * テーブルに新しい列 {@code column} を追加する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param column       列の名前。
     * @param type         列の型。
     * @param defaultValue デフォルトの値。必要ない場合はnullを指定する。
     * @param showWarning  同じ列が存在したときにコンソールに警告を表示するかどうか
     *
     */
    public void addColumn(String column, String type, String defaultValue, boolean showWarning) {
        if (getColumnMap().keySet().contains(column)) {
            if (showWarning) {
                log.warning(":COLUMN_EXIST");
            }

            return;
        }

        defaultValue = (defaultValue != null) ? " NOT NULL DEFAULT '" + defaultValue + "'" : "";
        val statement = prepare("ALTER TABLE " + table + " ADD " + column + " " + type + defaultValue);

        statement.map(stmt -> {
            try {
                stmt.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(stmt));

                return true;
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        });
    }

    /**
     * テーブル {@code table} から列 {@code column} を削除する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param column 削除する列の名前。
     *
     */
    public void dropColumn(String column) {
        if (!getColumnMap().keySet().contains(column)) {
            log.warning(":NO_COLUMN_NAMED_" + column + "_EXIST");

            return;
        }

        // 新しいテーブルの列
        val columnsBuilder = new StringBuilder();

        getColumnMap().forEach((colName, colType) -> {
            if (!column.equals(colName)) {
                columnsBuilder.append(colName).append(" ").append(colType).append(", ");
            }
        });

        val columns = columnsBuilder.toString().replaceAll(", $", "");

        // 新しいテーブルの列 (型なし)
        val columnsBuilderExcludeType = new StringBuilder();

        getColumnMap().forEach((colName, colType) -> {
            if (!column.equals(colName))
                columnsBuilderExcludeType.append(colName).append(", ");
        });

        val columnsExcludeType = columnsBuilderExcludeType.toString().replaceAll(", $", "");

        connection.ifPresent(con -> {
            try (val statement = con.createStatement()) {
                statement.addBatch("BEGIN TRANSACTION");
                statement.addBatch("ALTER TABLE " + table + " RENAME TO temp_" + table + "");
                statement.addBatch("CREATE TABLE " + table + " (" + columns + ")");
                statement.addBatch("INSERT INTO " + table + " (" + columnsExcludeType + ") SELECT " + columnsExcludeType
                                + " FROM temp_" + table + "");
                statement.addBatch("DROP TABLE temp_" + table + "");
                statement.addBatch("COMMIT");

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });
    }

    /**
     * エントリーの複数のカラムの値を一気に取得する。マップはLinkedHashMapで、引数のListの順番を引き継ぐ。
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     *
     * @return カラムと値のマップ
     */
    public Map<String, String> getMultiValue(List<String> columns, @NonNull String entry) {
        val entryType = Commands.checkEntryType(entry);

        val sb = new StringBuilder();

        for (String columnName : columns) {
            sb.append(columnName).append(", ");
        }

        val multipleColumnName = sb.substring(0, sb.length() - 2);

        val statement = prepare("SELECT " + multipleColumnName + " FROM " + table + " WHERE " + entryType + " = ?");

        return statement.map(resource -> {
            try (PreparedStatement stmt = resource) {
                stmt.setString(1, entry);
                ResultSet rs = stmt.executeQuery();

                return columns.stream()
                        .collect(Collectors.toMap(
                                columnName -> columnName,
                                columnName -> {
                                    try {
                                        return rs.getString(columnName);
                                    } catch (SQLException exception) {
                                        exception.printStackTrace();

                                        return "";
                                    }
                                },
                                (e1, e2) -> e1, LinkedHashMap::new)
                        );
            } catch (SQLException exception) {
                exception.printStackTrace();

                return new LinkedHashMap<String, String>();
            }
        }).get();
    }

    /**
     * エントリーの複数のカラムの値を一気に取得する
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     *
     */
    public void setMultiValue(Map<String, String> columnValueMap, @NonNull String entry) {
        val entryType = Commands.checkEntryType(entry);

        val sb = new StringBuilder();

        columnValueMap.forEach((columnName, columnValue) ->
                sb.append(columnName).append(" = '").append(columnValue).append("', ")
        );

        val statement = prepare(
                "UPDATE " + table + " SET " + sb.substring(0, sb.length() - 2) + " WHERE " + entryType + " = ?"
        );

        statement.map(resource -> {
            try (PreparedStatement stmt = resource) {
                stmt.setString(1, entry);
                stmt.executeUpdate();
                return true;
            } catch (SQLException exception) {
                exception.printStackTrace();
                return false;
            }
        });
    }

    /**
     * テーブルに含まれる列 {@code column} のリストを取得する。
     *
     * @author LazyGon
     * @since 1.0.0-SNAPSHOT
     *
     *
     * @return テーブルに含まれるcolumnの名前と型のマップ 失敗したら空のマップを返す。
     */
    public Map<String, String> getColumnMap() {
        val columnMap = new HashMap<String, String>();

        val statement = prepare("SELECT * FROM " + table + " WHERE 0=1");

        return statement.map(resource -> {
            try (val stmt = resource) {
                val resultMeta = stmt.executeQuery().getMetaData();

                for (int i = 1; i <= resultMeta.getColumnCount(); i++) {
                    columnMap.put(resultMeta.getColumnName(i), resultMeta.getColumnTypeName(i));
                }

                return columnMap;
            } catch (SQLException exception) {
                exception.printStackTrace();

                return new HashMap<String, String>();
            }
        }).orElse(columnMap);
    }

    /**
     * 登録されているプレイヤーの名前とUUIDのマップを取得する。
     *
     * @author LazyGon
     * @since 1.1.0-SNAPSHOT
     *
     * @return プレイヤー名とそのUUIDのマップ
     */
    public Map<String, String> getPlayersMap() {
        val playersMap = new HashMap<String, String>();
        val statement = prepare("SELECT uuid, player FROM " + table);

        statement.ifPresent(resource -> {
            try (val stmt = resource) {
                val result = stmt.executeQuery();

                while (result.next()) {
                    playersMap.put(result.getString("uuid"), result.getString("player"));
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        if (playersMap.isEmpty()) {
            log.warning(":MAP_IS_EMPTY");
        }

        return playersMap;
    }

    /**
     * 非推奨。NULLにしてもなんの意味もない。どころかエラーを引き起こす可能性まである。 {@code table} の {@code column} の
     * {@code entry} の行をNULLにする。(消す)
     *
     * @author LazyGon
     * @since 1.1.0-SNAPSHOT
     *
     * @param column 列
     * @param entry  エントリ
     */
    @Deprecated
    public boolean removeValue(String column, String entry) {
        if (!getColumnMap().keySet().contains(column)) {
            log.warning(":NO_COLUMN_NAMED_" + column + "_EXIST");

            return false;
        }

        if (!existPlayer(entry)) {
            log.warning(":NO_RECORD_FOR_" + entry + "_EXIST");

            return false;
        }

        val entryType = Commands.checkEntryType(entry);
        val statement = prepare("UPDATE " + table + " SET " + column + " = NULL WHERE " + entryType + " = ?");

        return statement.map(resource -> {
            try (val stmt = resource) {
                stmt.setString(1, entry);
                stmt.executeUpdate();

                return true;
            } catch (SQLException exception) {
                exception.printStackTrace();

                return false;
            }
        }).orElse(false);
    }

    /**
     * SQL 準備文を構築する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param sql SQL 文。
     *
     * @return SQL 準備文
     */
    private Optional<PreparedStatement> prepare(@NonNull String sql) {
        if (connection.isPresent()) {
            try {
                return Optional.of(connection.get().prepareStatement(sql));
            } catch (SQLException e) {
                e.printStackTrace();

                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    /**
     * Connection(String, Properties)} のラッパーメソッド。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     *
     * @see DriverManager#getConnection(String, Properties)
     *
     * @param url   {@code jdbc:subprotocol:subname} という形式のデータベース URL
     * @param props データベースの取り扱いについてのプロパティ
     *
     * @return 指定されたデータベースへの接続 {@code Connect} 。
     */
    private static Optional<Connection> getConnection(@NonNull String url, Properties props) {
        try {
            return Optional.of(DriverManager.getConnection(url, props));
        } catch (SQLException exception) {
            exception.printStackTrace();

            return Optional.empty();
        }
    }
}
