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
    void testPlayerUsesPersistentIdentity() {
        var player = mock(Player.class);
        var id = new UUID(0, 1);
        when(player.getUniqueId()).thenReturn(id);

        assertEquals(new StockActor.Player(id), StockActor.fromSender(player));
    }

    @Test
    void testConsoleAndRemoteConsoleAreConsoleActors() {
        assertEquals(StockActor.Console.INSTANCE, StockActor.fromSender(mock(ConsoleCommandSender.class)));
        assertEquals(StockActor.Console.INSTANCE, StockActor.fromSender(mock(RemoteConsoleCommandSender.class)));
    }

    @Test
    void testCommandBlockIsNotAttributedToConsole() {
        assertNull(StockActor.fromSender(mock(BlockCommandSender.class)));
    }
}
