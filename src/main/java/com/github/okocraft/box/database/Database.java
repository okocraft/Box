package com.github.okocraft.box.database;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.bukkit.plugin.Plugin;

import java.util.Map;
import java.util.HashMap;
import java.util.List;

import lombok.Getter;
import lombok.NonNull;
import lombok.val;

import com.github.okocraft.box.Box;
import com.github.okocraft.box.command.Commands;

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
     * データベースのファイル自体が存在しない場合はファイルを作成する。 ファイル内になんらデータベースが存在しない場合、データベースを新たに生成する。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     */
    public boolean connect(String url) {
        // Check if driver exists
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException exception) {
            //log.error("There's no JDBC driver.");
            log.severe("There's no JDBC driver.");
            exception.printStackTrace();

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
        } catch (IOException exception) {
            //log.error("Failed to create database file.");
            log.severe("Failed to create database file.");
            exception.printStackTrace();

            return false;
        }

        // Connect to database
        connection = getConnection(DBUrl, DBProps);

        if (!connection.isPresent()) {
            //log.error("Failed to connect the database.");
            log.severe("Failed to connect the database.");

            return false;
        }

        // create table for Box plugin
        boolean isTableCreated = connection.map(connection -> {
            try {
                connection.createStatement().execute(
                        "CREATE TABLE IF NOT EXISTS " + table + " (uuid TEXT PRIMARY KEY NOT NULL, player TEXT NOT NULL, autostore TEXT NOT NULL)");

                return true;
            } catch (SQLException e) {
                e.printStackTrace();

                return false;
            }
        }).orElse(false);

        if (!isTableCreated){
            log.severe("Failed to create the table.");
            return false;
        }

        List<String> allItems = Box.getInstance().getConfigManager().getAllItems();
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
     * データベースにレコードを追加する。
     * showWarningがtrueで失敗した場合はコンソールにログを出力する。
     *
     * @since 1.0.0-SNAPSHOT
     * @author akaregi
     *
     * @param uuid UUID
     * @param name 名前
     * @param showWarning コンソールログを出力するかどうか
     * 
     * @return 成功すればtrue 失敗すればfalse
     */
    public boolean addPlayer(@NonNull String uuid, @NonNull String name, boolean showWarning) {

        if (existPlayer(name)){
            if (showWarning) log.warning(":RECORD_EXIST");
            return false;
        }

        if (Commands.checkEntryType(uuid).equals("player")) {
            if (showWarning) log.warning(":INVALID_UUID");
            return false;
        }

        if (!name.matches("(\\d|[a-zA-Z]|_){3,16}")) {
            if (showWarning) log.warning(":INVALID_NAME");
            return false;
        }

        return prepare("INSERT OR IGNORE INTO " + table + " (uuid, player) VALUES (?, ?)").map(statement -> {
            try {
                statement.setString(1, uuid);
                statement.setString(2, name);
                statement.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }).orElse(false);
    }

    /**
     * テーブルからレコードを削除する。
     * 失敗した場合はコンソールにログを出力する。
     *
     * @since 1.1.0-SNAPSHOT
     * @author LazyGon
     *
     * @param entry プレイヤー
     * 
     * @return 成功すればtrue 失敗すればfalse
     */
    public boolean removePlayer(@NonNull String entry) {

        if (!existPlayer(entry)){
            log.warning(":NO_RECORD_EXIST");
            return false;
        }

        String entryType = Commands.checkEntryType(entry);

        return prepare("DELETE FROM " + table + " WHERE " + entryType + " = ?").map(statement -> {
            try {
                statement.setString(1, entry);
                statement.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(statement));
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }).orElse(false);
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
        Map<String, String> playersMap = getPlayersMap();
        return playersMap.containsKey(entry) || playersMap.containsValue(entry);
    }

    /**
     * {@code table}の{@code column}に値をセットする。
     *
     * @since 1.0.0-SNAPSHOT
     * @author LazyGon
     *
     * @param column 更新する列
     * @param entry  プレイヤー。uuidでもmcidでも可
     * @param value  新しい値
     */
    public boolean set(@NonNull String column, @NonNull String entry, String value) {

        if (!getColumnMap().keySet().contains(column)){
            log.warning(":COLUMN_NOT_EXIST");
            return false;
        }

        if (!existPlayer(entry)){
            log.warning(":RECORD_NOT_EXIST");
            return false;
        }

        String entryType = Commands.checkEntryType(entry);

        return prepare("UPDATE " + table + " SET " + column + " = ? WHERE " + entryType + " = ?")
                .map(statement -> {
                    try {
                        statement.setString(1, value);
                        statement.setString(2, entry);
                        statement.addBatch();

                        // Execute this batch
                        threadPool.submit(new StatementRunner(statement));
                        return true;
                    } catch (SQLException e) {
                        e.printStackTrace();
                        return false;
                    }
                }).orElse(false);
    }

    /**
     * {@code table} で指定したテーブルの列 {@code column} の値を取得する。
     * テーブル、カラム、レコードのいずれかが存在しない場合は対応するエラー文字列を返す。
     * 
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     * 
     * @param table
     * @param column
     * @param entry
     * @return 値
     */
    public String get(String column, String entry) {

        if (!getColumnMap().keySet().contains(column))
            return ":COLUMN_NOT_EXIST";

        if (!existPlayer(entry))
            return ":RECORD_NOT_EXIST";

        String entryType = Commands.checkEntryType(entry);

        val statement = prepare("SELECT " + column + " FROM " + table + " WHERE " + entryType + " = ?");

        Optional<String> result = statement.map(stmt -> {
            try {
                stmt.setString(1, entry);
                ResultSet rs = stmt.executeQuery();
                return rs.getString(column);
            } catch (SQLException exception) {
                exception.printStackTrace();

                return "";
            }
        });

        return result.orElse(":NOTHING");
    }

    /**
     * テーブルに新しい列 {@code column} を追加する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param column 列の名前。
     * @param type   列の型。
     * @param defaultValue デフォルトの値。必要ない場合はnullを指定する。
     * @param showWarning 同じ列が存在したときにコンソールに警告を表示するかどうか
     *
     * @return 成功したなら {@code true} 、さもなくば {@code false} 。
     */
    public boolean addColumn(String column, String type, String defaultValue, boolean showWarning) {

        if (getColumnMap().keySet().contains(column)){
            if (showWarning) log.warning(":COLUMN_EXIST");
            return false;
        }

        defaultValue =  (defaultValue != null) ? " NOT NULL DEFAULT '" + defaultValue + "'" : "";
        val statement = prepare(
                "ALTER TABLE " + table + " ADD " + column + " " + type + defaultValue);

        return statement.map(stmt -> {
            try {
                stmt.addBatch();

                // Execute this batch
                threadPool.submit(new StatementRunner(stmt));
                return true;
            } catch (SQLException exception) {
                exception.printStackTrace();
                return false;
            }
        }).orElse(false);
    }

    /**
     * テーブル {@code table} から列 {@code column} を削除する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param column 削除する列の名前。
     *
     * @return 成功したなら {@code true} 、さもなくば {@code false} 。
     */
    public boolean dropColumn(String column) {

        if (!getColumnMap().keySet().contains(column)){
            log.warning(":COLUMN_NOT_EXIST");
            return false;
        }

        // 新しいテーブルの列
        StringBuilder columnsBuilder = new StringBuilder();
        getColumnMap().forEach((colName, colType) -> {
            if (!column.equals(colName))
                columnsBuilder.append(colName + " " + colType + ", ");
        });
        String columns = columnsBuilder.toString().replaceAll(", $", "");

        // 新しいテーブルの列 (型なし)
        StringBuilder colmunsBuilderExcludeType = new StringBuilder();
        getColumnMap().forEach((colName, colType) -> {
            if (!column.equals(colName))
                colmunsBuilderExcludeType.append(colName + ", ");
        });
        String columnsExcludeType = colmunsBuilderExcludeType.toString().replaceAll(", $", "");

        Statement statement;

        try {
            statement = connection.get().createStatement();

            statement.addBatch("BEGIN TRANSACTION");
            statement.addBatch("ALTER TABLE " + table + " RENAME TO temp_" + table + "");
            statement.addBatch("CREATE TABLE " + table + " (" + columns + ")");
            statement.addBatch("INSERT INTO " + table + " (" + columnsExcludeType + ") SELECT "
                    + columnsExcludeType + " FROM temp_" + table + "");
            statement.addBatch("DROP TABLE temp_" + table + "");
            statement.addBatch("COMMIT");

            // Execute this batch
            threadPool.submit(new StatementRunner(statement));
            return true;
        } catch (SQLException exception) {
            exception.printStackTrace();
            return false;
        }
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
        
        Map<String, String> columnMap = new HashMap<>();

        val statement = prepare("SELECT * FROM " + table + " WHERE 0=1");

        return statement.map(stmt -> {
            try {
                ResultSetMetaData rsmd = stmt.executeQuery().getMetaData();

                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    columnMap.put(rsmd.getColumnName(i), rsmd.getColumnTypeName(i));
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

        Map<String, String> playersMap = new HashMap<>();

        val statement = prepare("SELECT uuid, player FROM " + table);

        statement.ifPresent(stmt -> {
            try {
                ResultSet rs = stmt.executeQuery();
                while (rs.next())
                    playersMap.put(rs.getString("uuid"), rs.getString("player"));
            } catch (SQLException exception) {
                exception.printStackTrace();
            }
        });

        if (playersMap.isEmpty())
            log.warning(":MAP_IS_EMPTY");
        return playersMap;
    }

    /**
     * {@code table} の {@code column} の {@code entry} の行をNULLにする。(消す)
     * 
     * @author LazyGon
     * @since 1.1.0-SNAPSHOT
     * 
     * @param table
     * @param column
     * @param entry
     */
    public boolean removeValue(String column, String entry) {

        if (!getColumnMap().keySet().contains(column)){
            log.warning(":COLUMN_NOT_EXIST");
            return false;
        }

        if (!existPlayer(entry)){
            log.warning(":RECORD_NOT_EXIST");
            return false;
        }

        String entryType = Commands.checkEntryType(entry);

        val statement = prepare("UPDATE " + table + " SET " + column + " = NULL WHERE " + entryType + " = ?");

        return statement.map(stmt -> {
            try {
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
     * スレッド上で SQL を実行する。
     *
     * @author akaregi
     * @since 1.0.0-SNAPSHOT
     *
     * @param statement SQL 準備文。
     *
     * @return {@Code ResultSet}
     */
    public Optional<ResultSet> exec(PreparedStatement statement) {
        val thread = threadPool.submit(new StatementCaller(statement));

        try {
            return thread.get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();

            return Optional.empty();
        }
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
    public Optional<PreparedStatement> prepare(@NonNull String sql) {
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
