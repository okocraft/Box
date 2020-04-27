package net.okocraft.box.database;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

class Database {

    /** データベースのコネクションプール。 */
    private final HikariDataSource hikari;

    /** SQLiteかどうか。 */
    private final boolean isSQLite;

    /**
     * 初期設定でSQLiteに接続する。
     * 
     * @param dbPath SQLiteのデータファイルのパス
     * @throws SQLException {@code Connection}の生成中に例外が発生した場合
     */
    Database(Path dbPath) throws SQLException {
        HikariConfig config = new HikariConfig();
        dbPath.toFile().getParentFile().mkdirs();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + dbPath.toFile().getPath());
        hikari = new HikariDataSource(config);
        isSQLite = true;
    }

    /**
     * 推奨設定でMySQLに接続する。
     * 参照: https://github.com/brettwooldridge/HikariCP/wiki/MySQL-Configuration
     * 
     * @param host     ホスト
     * @param port     ポート
     * @param user     ユーザー
     * @param password パスワード
     * @param dbName   データベースの名前
     * @throws SQLException {@code Connection}の生成中に例外が発生した場合
     */
    Database(String host, int port, String user, String password, String dbName) throws SQLException {
        HikariConfig config = new HikariConfig();

        // login data
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + dbName + "?autoReconnect=true&allowPublicKeyRetrieval=true&useSSL=false");
        config.setUsername(user);
        config.setPassword(password);

        // general mysql settings
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("prepStmtsCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("useServerPrepStmts", true);
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("rewriteBatchedStatements", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("maintainTimeStats", false);
        hikari = new HikariDataSource(config);
        isSQLite = false;
    }

    public boolean isSQLite() {
        return isSQLite;
    }

    /**
     * 指定した {@code statement}を実行する。
     * 
     * @param SQL 実行するSQL文。メソッド内でPreparedStatementに変換される。
     * @return SQL文の実行に成功したかどうか
     */
    boolean execute(String SQL) {
        try (PreparedStatement preparedStatement = hikari.getConnection().prepareStatement(SQL)) {
            preparedStatement.execute();
            return true;
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + SQL);
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 指定した {@code statement}を実行し、結果を第二引数で処理する。第二引数の処理が終わった後に、ResultSetはクローズされる。
     * 
     * @param SQL 実行するSQL文。メソッド内でPreparedStatementに変換される。
     * @param function 実行結果を受け取る関数。
     * @return fuctionの処理結果
     */
    <T> T query(String SQL, Function<ResultSet, T> function) {
        try (PreparedStatement preparedStatement = hikari.getConnection().prepareStatement(SQL)) {
            return function.apply(preparedStatement.executeQuery());
        } catch (SQLException e) {
            System.err.println("Error occurred on executing SQL: " + SQL);
            e.printStackTrace();
            return null;
        }
    }

    Connection getConnection() {
        try {
            return hikari.getConnection();
        } catch (SQLException e) {
            return null;
        }
    }

    /**
     * データベースのコネクションプールやコネクションを閉じる。
     */
    void dispose() {
        if (hikari != null) {
            hikari.close();
        }
    }
}