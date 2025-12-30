package net.okocraft.box.feature.category.internal.listener;

import dev.siroshun.configapi.core.node.MapNode;
import dev.siroshun.configapi.format.yaml.YamlFormat;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.event.item.CustomItemRegisterEvent;
import net.okocraft.box.api.event.item.CustomItemRenameEvent;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.api.util.SubscribedListenerHolder;
import net.okocraft.box.feature.category.api.registry.CategoryRegistry;
import net.okocraft.box.feature.category.internal.category.CustomItemCategory;
import net.okocraft.box.feature.category.internal.file.CategoryFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class CustomItemListener {

    private final Path filepath;
    private final CategoryRegistry registry;
    private final SubscribedListenerHolder listenerHolder = new SubscribedListenerHolder();

    public CustomItemListener(@NotNull Path filepath, @NotNull CategoryRegistry registry) {
        this.filepath = filepath;
        this.registry = registry;
    }

    public void register(@NotNull Key listenerKey) {
        this.listenerHolder.subscribeAll(subscriber ->
            subscriber.add(CustomItemRegisterEvent.class, listenerKey, this::processEvent)
                .add(CustomItemRenameEvent.class, listenerKey, this::processEvent)
        );
    }

    public void unregister() {
        this.listenerHolder.unsubscribeAll();
    }

    private void processEvent(@NotNull CustomItemRegisterEvent event) {
        this.registry.getCustomItemCategory().addItem(event.getItem());

        try {
            this.addItemToCustomItems(event.getItem());
        } catch (IOException e) {
            BoxLogger.logger().error("Could not save categories.yml", e);
        }
    }

    private void processEvent(@NotNull CustomItemRenameEvent event) {
        try {
            this.updateCategoriesFile(event.getPreviousName(), event.getItem().getPlainName());
        } catch (IOException e) {
            BoxLogger.logger().error("Could not save categories.yml", e);
        }
    }

    private void updateCategoriesFile(@NotNull String oldName, @NotNull String newName) throws IOException {
        MapNode mapNode = YamlFormat.COMMENT_PROCESSING.load(this.filepath);

        for (Object key : mapNode.value().keySet()) {
            if (key.equals(CustomItemCategory.CONFIG_KEY)) {
                List<String> newList = renameItem(oldName, newName, mapNode.getList(key).asList(String.class));

                if (newList != null) {
                    mapNode.set(key, newList);
                }
            } else if (!String.valueOf(key).startsWith("$")) {
                MapNode map = mapNode.getMap(key);
                List<String> newList = renameItem(oldName, newName, map.getList(CategoryFile.ITEMS_KEY).asList(String.class));

                if (newList != null) {
                    map.set(CategoryFile.ITEMS_KEY, newList);
                }
            }
        }

        YamlFormat.COMMENT_PROCESSING.save(mapNode, this.filepath);
    }

    private static @Nullable List<String> renameItem(@NotNull String oldName, @NotNull String newName, List<String> itemNameList) {
        List<String> newList = null;

        for (int i = 0, size = itemNameList.size(); i < size; i++) {
            if (oldName.equals(itemNameList.get(i))) {
                if (newList == null) {
                    newList = new ArrayList<>(size);
                    if (i != 0) {
                        newList.addAll(itemNameList.subList(0, i));
                    }
                }

                newList.add(newName);
            }
        }

        return newList;
    }

    private void addItemToCustomItems(@NotNull BoxItem item) throws IOException {
        MapNode loaded = YamlFormat.COMMENT_PROCESSING.load(this.filepath);
        loaded.getOrCreateList(CustomItemCategory.CONFIG_KEY).add(item.getPlainName());
        YamlFormat.COMMENT_PROCESSING.save(loaded, this.filepath);
    }
}
