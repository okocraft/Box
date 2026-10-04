package net.okocraft.box.storage.implementation.database.database.sqlite;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLClientInfoException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.ShardingKey;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * A handle that closes its lease without closing the database's shared connection.
 * JDBC objects returned by the handle remain bound to the same lease.
 */
public class NonCloseableConnection implements Connection {

    private final Connection delegate;
    private final Runnable release;
    private volatile boolean released;
    private final Map<Object, Object> jdbcWrappers = new IdentityHashMap<>();

    public NonCloseableConnection(Connection delegate) {
        this(delegate, null);
    }

    NonCloseableConnection(Connection delegate, Runnable release) {
        this.delegate = delegate;
        this.release = release;
    }

    /**
     * Closes an independently wrapped connection. Shared leases cannot shut down
     * the database; that is the database owner's responsibility.
     */
    public final void shutdown() throws SQLException {
        this.checkOpen();
        if (this.release != null) {
            throw new SQLFeatureNotSupportedException("A shared SQLite lease cannot shut down the database.");
        }
        this.delegate.close();
        this.released = true;
        this.jdbcWrappers.clear();
    }

    @Override
    public final void close() throws SQLException {
        if (this.released) return;
        SQLException failure = null;
        try {
            for (Object resource : this.jdbcWrappers.keySet()) {
                try {
                    if (resource instanceof ResultSet rows) rows.close();
                    else if (resource instanceof Statement statement) statement.close();
                } catch (SQLException e) {
                    if (failure == null) failure = e;
                    else failure.addSuppressed(e);
                }
            }
        } finally {
            this.released = true;
            this.jdbcWrappers.clear();
            if (this.release != null) this.release.run();
        }
        if (failure != null) throw failure;
    }

    @Override
    public final boolean isWrapperFor(Class<?> iface) throws SQLException {
        this.checkOpen();
        return iface.isInstance(this);
    }

    @Override
    public final <T> T unwrap(Class<T> iface) throws SQLException {
        this.checkOpen();
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("The shared SQLite connection cannot be unwrapped as " + iface.getName());
    }

    private void checkOpen() throws SQLException {
        if (this.released || this.delegate.isClosed()) {
            throw new SQLException("The SQLite connection lease is closed.");
        }
    }

    private Connection connection() throws SQLException {
        this.checkOpen();
        return this.delegate;
    }

    private Connection clientInfoConnection() throws SQLClientInfoException {
        try {
            return this.connection();
        } catch (SQLException e) {
            throw new SQLClientInfoException("The SQLite connection lease is closed.", Map.of(), e);
        }
    }

    // Statements, result sets and metadata can otherwise expose the raw shared
    // connection through getConnection(), getStatement() or unwrap().
    @SuppressWarnings("unchecked")
    private <T> T wrapJdbc(T value) {
        if (value == null) return null;
        if (value instanceof Connection) return (T) this;
        Class<?> type;
        if (value instanceof CallableStatement) type = CallableStatement.class;
        else if (value instanceof PreparedStatement) type = PreparedStatement.class;
        else if (value instanceof Statement) type = Statement.class;
        else if (value instanceof ResultSet) type = ResultSet.class;
        else if (value instanceof DatabaseMetaData) type = DatabaseMetaData.class;
        else return value;

        return (T) this.jdbcWrappers.computeIfAbsent(value, delegate ->
            Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[]{type}, (proxy, method, args) -> {
                if (method.getDeclaringClass() == Object.class) {
                    return switch (method.getName()) {
                        case "equals" -> proxy == args[0];
                        case "hashCode" -> System.identityHashCode(proxy);
                        case "toString" -> "SQLite lease " + type.getSimpleName();
                        default -> throw new UnsupportedOperationException(method.getName());
                    };
                }
                if (method.getName().equals("isClosed") && this.released) return true;
                this.checkOpen();
                if (method.getName().equals("isWrapperFor")) {
                    return ((Class<?>) args[0]).isInstance(proxy);
                }
                if (method.getName().equals("unwrap")) {
                    Class<?> requested = (Class<?>) args[0];
                    if (requested.isInstance(proxy)) return proxy;
                    throw new SQLException("The shared SQLite JDBC object cannot be unwrapped as " + requested.getName());
                }
                try {
                    return this.wrapJdbc(method.invoke(delegate, args));
                } catch (InvocationTargetException e) {
                    throw e.getCause();
                }
            })
        );
    }

    // @formatter:off
    // Forward to the delegate connection
    @Override public Statement createStatement() throws SQLException { return this.wrapJdbc(this.connection().createStatement()); }
    @Override public PreparedStatement prepareStatement(String sql) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql)); }
    @Override public CallableStatement prepareCall(String sql) throws SQLException { return this.wrapJdbc(this.connection().prepareCall(sql)); }
    @Override public String nativeSQL(String sql) throws SQLException { return this.connection().nativeSQL(sql); }
    @Override public void setAutoCommit(boolean autoCommit) throws SQLException { this.connection().setAutoCommit(autoCommit); }
    @Override public boolean getAutoCommit() throws SQLException { return this.connection().getAutoCommit(); }
    @Override public void commit() throws SQLException { this.connection().commit(); }
    @Override public void rollback() throws SQLException { this.connection().rollback(); }
    @Override public boolean isClosed() throws SQLException { return this.released || this.delegate.isClosed(); }
    @Override public DatabaseMetaData getMetaData() throws SQLException { return this.wrapJdbc(this.connection().getMetaData()); }
    @Override public void setReadOnly(boolean readOnly) throws SQLException { this.connection().setReadOnly(readOnly); }
    @Override public boolean isReadOnly() throws SQLException { return this.connection().isReadOnly(); }
    @Override public void setCatalog(String catalog) throws SQLException { this.connection().setCatalog(catalog); }
    @Override public String getCatalog() throws SQLException { return this.connection().getCatalog(); }
    @Override public void setTransactionIsolation(int level) throws SQLException { this.connection().setTransactionIsolation(level); }
    @Override public int getTransactionIsolation() throws SQLException { return this.connection().getTransactionIsolation(); }
    @Override public SQLWarning getWarnings() throws SQLException { return this.connection().getWarnings(); }
    @Override public void clearWarnings() throws SQLException { this.connection().clearWarnings(); }
    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException { return this.wrapJdbc(this.connection().createStatement(resultSetType, resultSetConcurrency)); }
    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql, resultSetType, resultSetConcurrency)); }
    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException { return this.wrapJdbc(this.connection().prepareCall(sql, resultSetType, resultSetConcurrency)); }
    @Override public Map<String, Class<?>> getTypeMap() throws SQLException { return this.connection().getTypeMap(); }
    @Override public void setTypeMap(Map<String, Class<?>> map) throws SQLException { this.connection().setTypeMap(map); }
    @Override public void setHoldability(int holdability) throws SQLException { this.connection().setHoldability(holdability); }
    @Override public int getHoldability() throws SQLException { return this.connection().getHoldability(); }
    @Override public Savepoint setSavepoint() throws SQLException { return this.connection().setSavepoint(); }
    @Override public Savepoint setSavepoint(String name) throws SQLException { return this.connection().setSavepoint(name); }
    @Override public void rollback(Savepoint savepoint) throws SQLException { this.connection().rollback(savepoint); }
    @Override public void releaseSavepoint(Savepoint savepoint) throws SQLException { this.connection().releaseSavepoint(savepoint); }
    @Override public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return this.wrapJdbc(this.connection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability)); }
    @Override public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability)); }
    @Override public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException { return this.wrapJdbc(this.connection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability)); }
    @Override public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql, autoGeneratedKeys)); }
    @Override public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql, columnIndexes)); }
    @Override public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException { return this.wrapJdbc(this.connection().prepareStatement(sql, columnNames)); }
    @Override public Clob createClob() throws SQLException { return this.connection().createClob(); }
    @Override public Blob createBlob() throws SQLException { return this.connection().createBlob(); }
    @Override public NClob createNClob() throws SQLException { return this.connection().createNClob(); }
    @Override public SQLXML createSQLXML() throws SQLException { return this.connection().createSQLXML(); }
    @Override public boolean isValid(int timeout) throws SQLException { if (timeout < 0) throw new SQLException("Timeout cannot be negative."); return !this.released && this.delegate.isValid(timeout); }
    @Override public void setClientInfo(String name, String value) throws SQLClientInfoException { this.clientInfoConnection().setClientInfo(name, value); }
    @Override public void setClientInfo(Properties properties) throws SQLClientInfoException { this.clientInfoConnection().setClientInfo(properties); }
    @Override public String getClientInfo(String name) throws SQLException { return this.connection().getClientInfo(name); }
    @Override public Properties getClientInfo() throws SQLException { return this.connection().getClientInfo(); }
    @Override public Array createArrayOf(String typeName, Object[] elements) throws SQLException { return this.connection().createArrayOf(typeName, elements); }
    @Override public Struct createStruct(String typeName, Object[] attributes) throws SQLException { return this.connection().createStruct(typeName, attributes); }
    @Override public void setSchema(String schema) throws SQLException { this.connection().setSchema(schema); }
    @Override public String getSchema() throws SQLException { return this.connection().getSchema(); }
    @Override public void abort(Executor executor) throws SQLException { this.checkOpen(); throw new SQLFeatureNotSupportedException("A SQLite lease cannot abort the shared connection."); }
    @Override public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException { this.connection().setNetworkTimeout(executor, milliseconds); }
    @Override public int getNetworkTimeout() throws SQLException { return this.connection().getNetworkTimeout(); }
    @Override public void beginRequest() throws SQLException { this.connection().beginRequest(); }
    @Override public void endRequest() throws SQLException { this.connection().endRequest(); }
    @Override public boolean setShardingKeyIfValid(ShardingKey key, ShardingKey superKey, int timeout) throws SQLException { return this.connection().setShardingKeyIfValid(key, superKey, timeout); }
    @Override public boolean setShardingKeyIfValid(ShardingKey key, int timeout) throws SQLException { return this.connection().setShardingKeyIfValid(key, timeout); }
    @Override public void setShardingKey(ShardingKey key, ShardingKey superKey) throws SQLException { this.connection().setShardingKey(key, superKey); }
    @Override public void setShardingKey(ShardingKey key) throws SQLException { this.connection().setShardingKey(key); }
    // @formatter:on
}
