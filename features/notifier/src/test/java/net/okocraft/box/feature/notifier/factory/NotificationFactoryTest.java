package net.okocraft.box.feature.notifier.factory;

import net.kyori.adventure.text.Component;
import net.okocraft.box.test.shared.model.item.DummyItem;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static net.kyori.adventure.text.Component.text;
import static net.kyori.adventure.text.format.NamedTextColor.AQUA;
import static net.kyori.adventure.text.format.NamedTextColor.RED;
import static net.kyori.adventure.text.format.NamedTextColor.WHITE;
import static net.okocraft.box.feature.notifier.factory.NotificationFactory.COMMON_PARTS_1;
import static net.okocraft.box.feature.notifier.factory.NotificationFactory.COMMON_PARTS_2;
import static net.okocraft.box.feature.notifier.factory.NotificationFactory.COMMON_PARTS_3;

class NotificationFactoryTest {

    private static final DummyItem ITEM = new DummyItem(1, "dummy_item");
    private static final Component EXPECTED_INCREASED_NOTIFICATION = text().append(ITEM.getDisplayName(), COMMON_PARTS_1, text(10, WHITE), COMMON_PARTS_2, text("+5", AQUA), COMMON_PARTS_3).build();
    private static final Component EXPECTED_DECREASED_NOTIFICATION = text().append(ITEM.getDisplayName(), COMMON_PARTS_1, text(10, WHITE), COMMON_PARTS_2, text("-5", RED), COMMON_PARTS_3).build();

    @Test
    void testIncrements() {
        Assertions.assertEquals(
                EXPECTED_INCREASED_NOTIFICATION,
                new NotificationFactory(ITEM, 10).increments(5).createNotification()
        );
    }

    @Test
    void testDecrements() {
        Assertions.assertEquals(
                EXPECTED_DECREASED_NOTIFICATION,
                new NotificationFactory(ITEM, 10).decrements(5).createNotification()
        );
    }

    @Test
    void testPrevious() {
        Assertions.assertEquals(
                EXPECTED_INCREASED_NOTIFICATION,
                new NotificationFactory(ITEM, 10).previous(5).createNotification()
        );

        Assertions.assertEquals(
                EXPECTED_DECREASED_NOTIFICATION,
                new NotificationFactory(ITEM, 10).previous(15).createNotification()
        );
    }
}
