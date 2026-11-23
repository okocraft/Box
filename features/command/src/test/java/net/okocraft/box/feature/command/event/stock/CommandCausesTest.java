package net.okocraft.box.feature.command.event.stock;

import net.okocraft.box.api.event.stockholder.stock.StockActor;
import net.okocraft.box.api.player.BoxPlayer;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommandCausesTest {

    @Test
    void testGiveAndReceiveBothIdentifyTheInitiatingPlayer() {
        var uuid = new UUID(0, 1);
        var player = mock(Player.class);
        when(player.getUniqueId()).thenReturn(uuid);
        when(player.getName()).thenReturn("Sender");
        var sender = mock(BoxPlayer.class);
        when(sender.getPlayer()).thenReturn(player);
        var target = mock(BoxPlayer.class);

        var give = new CommandCauses.Give(target, StockActor.fromPlayer(player));
        var receive = new CommandCauses.Receive(sender);

        var expected = new StockActor.Player(uuid, "Sender");
        assertEquals(expected, give.actor());
        assertEquals(expected, receive.actor());
        assertSame(target, give.target());
        assertNull(new CommandCauses.Give(target).actor());
    }
}
