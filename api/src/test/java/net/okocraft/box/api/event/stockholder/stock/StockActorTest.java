package net.okocraft.box.api.event.stockholder.stock;

import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StockActorTest {

    @Test
    void testPlayerIdentityRequiresUuidAndName() {
        var uuid = new UUID(0, 1);
        var player = new StockActor.Player(uuid, "ExamplePlayer");

        assertTrue(player.isPlayer());
        assertEquals(uuid, player.uuid());
        assertEquals("ExamplePlayer", player.name());
        assertThrows(NullPointerException.class, () -> new StockActor.Player(null, "ExamplePlayer"));
        assertThrows(NullPointerException.class, () -> new StockActor.Player(uuid, null));
    }

    @Test
    void testConsoleDoesNotRequireUuid() {
        assertFalse(StockActor.CONSOLE.isPlayer());
        assertNull(StockActor.CONSOLE.uuid());
        assertEquals("console", StockActor.CONSOLE.name());
    }

    @Test
    void testKnownCommandSendersHaveAnActor() {
        var player = mock(Player.class);
        var uuid = new UUID(0, 3);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("ExamplePlayer");

        assertEquals(new StockActor.Player(uuid, "ExamplePlayer"), StockActor.fromSender(player));
        assertSame(StockActor.CONSOLE, StockActor.fromSender(mock(ConsoleCommandSender.class)));
        assertSame(StockActor.CONSOLE, StockActor.fromSender(mock(RemoteConsoleCommandSender.class)));
        assertNull(StockActor.fromSender(mock(BlockCommandSender.class)));
    }

    @Test
    void testCauseAcceptsCustomNonPlayerActorWithUuid() {
        var uuid = new UUID(0, 2);
        record Automation(UUID uuid, String name) implements StockActor {}
        var actor = new Automation(uuid, "restock-task");

        var cause = StockEvent.Cause.create("restock", actor);

        assertSame(actor, cause.actor());
        assertFalse(actor.isPlayer());
        assertEquals(uuid, actor.uuid());
    }

    @Test
    void testExistingCausesRemainUnattributed() {
        StockEvent.Cause customCause = () -> "custom";

        assertNull(customCause.actor());
        assertNull(StockEvent.Cause.create("api").actor());
    }
}
