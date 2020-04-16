package net.okocraft.box;

import net.okocraft.box.config.Config;
import net.okocraft.box.config.Messages;

public final class BoxAPI {

    private final Config config = new Config();
    private final Messages messages = new Messages();

    BoxAPI() {
    }

    public Config getConfig() {
        return config;
    }

    public Messages getMessages() {
        return messages;
    }
}