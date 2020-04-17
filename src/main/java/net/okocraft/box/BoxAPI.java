package net.okocraft.box;

import net.okocraft.box.config.Categories;
import net.okocraft.box.config.Config;
import net.okocraft.box.config.Layouts;
import net.okocraft.box.config.Messages;
import net.okocraft.box.config.Prices;

public final class BoxAPI {

    private final Config config = new Config();
    private final Messages messages = new Messages();
    private final Layouts layout = new Layouts();
    private final Prices prices = new Prices();
    private final Categories categories = new Categories();

    BoxAPI() {
    }

    public Config getConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }

    public Layouts getLayouts() {
        return layout;
    }

    public Prices getPrices() {
        return prices;
    }

    public Categories getCategories() {
        return categories;
    }
}