package net.okocraft.box.test.shared.model.user;

import net.okocraft.box.api.model.user.BoxUser;
import net.okocraft.box.storage.api.factory.user.BoxUserFactory;

import java.util.UUID;

public final class TestUser {

    public static final BoxUser USER = BoxUserFactory.create(UUID.randomUUID(), "test_user");

    private TestUser() {
        throw new UnsupportedOperationException();
    }
}
