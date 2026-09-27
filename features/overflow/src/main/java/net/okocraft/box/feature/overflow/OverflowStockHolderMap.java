package net.okocraft.box.feature.overflow;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.core.node.Node;
import dev.siroshun.configapi.core.node.StringRepresentable;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.model.customdata.CustomDataManager;
import net.okocraft.box.api.util.BoxLogger;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

final class OverflowStockHolderMap {

    private static final Key DATA_KEY = Key.key("overflow", "stock-holder-map");
    private static final String HOLDERS_KEY = "holders";

    private final CustomDataManager customDataManager;
    private final Map<UUID, UUID> nextHolderBySource = new HashMap<>();
    private final Set<UUID> overflowHolders = new HashSet<>();

    OverflowStockHolderMap(@NotNull CustomDataManager customDataManager) throws Exception {
        this.customDataManager = customDataManager;
        this.load();
    }

    synchronized boolean isOverflowHolder(@NotNull UUID uuid) {
        return this.overflowHolders.contains(uuid);
    }

    synchronized @NotNull UUID getOrCreateNext(@NotNull UUID sourceUuid) throws Exception {
        UUID existing = this.nextHolderBySource.get(sourceUuid);
        if (existing != null) {
            return existing;
        }

        UUID nextUuid;
        do {
            nextUuid = UUID.randomUUID();
        } while (this.nextHolderBySource.containsKey(nextUuid) || this.overflowHolders.contains(nextUuid));

        this.nextHolderBySource.put(sourceUuid, nextUuid);
        this.overflowHolders.add(nextUuid);

        try {
            this.save();
        } catch (Exception e) {
            this.nextHolderBySource.remove(sourceUuid);
            this.overflowHolders.remove(nextUuid);
            throw e;
        }

        return nextUuid;
    }

    private void load() throws Exception {
        MapNode data = this.customDataManager.loadData(DATA_KEY);
        Node<?> holdersNode = data.get(HOLDERS_KEY);

        if (!(holdersNode instanceof MapNode holders)) {
            return;
        }

        for (Map.Entry<Object, Node<?>> entry : holders.value().entrySet()) {
            if (!(entry.getValue() instanceof StringRepresentable targetValue)) {
                continue;
            }

            UUID sourceUuid;
            UUID targetUuid;

            try {
                sourceUuid = UUID.fromString(String.valueOf(entry.getKey()));
                targetUuid = UUID.fromString(targetValue.asString());
            } catch (IllegalArgumentException e) {
                BoxLogger.logger().warn(
                    "Ignoring invalid overflow stock holder mapping '{} -> {}'.",
                    entry.getKey(),
                    targetValue.asString()
                );
                continue;
            }

            if (sourceUuid.equals(targetUuid)
                || this.overflowHolders.contains(targetUuid)
                || this.createsCycle(sourceUuid, targetUuid)) {
                BoxLogger.logger().warn(
                    "Ignoring invalid overflow stock holder mapping '{} -> {}'.",
                    sourceUuid,
                    targetUuid
                );
                continue;
            }

            this.nextHolderBySource.put(sourceUuid, targetUuid);
            this.overflowHolders.add(targetUuid);
        }
    }

    private boolean createsCycle(@NotNull UUID sourceUuid, @NotNull UUID targetUuid) {
        UUID current = targetUuid;

        while (current != null) {
            if (current.equals(sourceUuid)) {
                return true;
            }
            current = this.nextHolderBySource.get(current);
        }

        return false;
    }

    private void save() throws Exception {
        MapNode data = MapNode.create();
        MapNode holders = data.createMap(HOLDERS_KEY);

        this.nextHolderBySource.forEach((source, target) ->
            holders.set(source.toString(), target.toString())
        );

        this.customDataManager.saveData(DATA_KEY, data);
    }
}
