package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNullByDefault;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@NotNullByDefault
public final class BulkInserter {

    private final String baseSql;
    private final String parameters;

    private BulkInserter(String baseSql, String parameters) {
        this.baseSql = baseSql;
        this.parameters = parameters;
    }

    public static BulkInserter create(String tableName, List<String> columns) {
        return new BulkInserter(
            "INSERT INTO " + tableName + " (" + columns.stream().map(column -> "`" + column + "`").collect(Collectors.joining(", ")) + ") VALUES ",
            "(" + IntStream.range(0, columns.size()).mapToObj(ignored -> "?").collect(Collectors.joining(", ")) + ")"
        );
    }

    public String createQuery(int records) {
        if (records == 0) {
            throw new IllegalArgumentException("records cannot be zero");
        }
        StringBuilder builder = new StringBuilder(this.baseSql);
        for (int i = 0; i < records; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(this.parameters);
        }
        return builder.toString();
    }
}
