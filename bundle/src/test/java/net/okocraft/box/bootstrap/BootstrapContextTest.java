package net.okocraft.box.bootstrap;

import net.okocraft.box.api.bootstrap.BootstrapContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BootstrapContextTest {
    @Test
    void testGet() {
        try (var mock = Mockito.mockStatic(BoxBootstrap.class)) {
            var bootstrap = Mockito.mock(BoxBootstrap.class);
            var context = Mockito.mock(BoxBootstrapContext.class);
            Mockito.when(bootstrap.getContext()).thenReturn(context);
            mock.when(BoxBootstrap::get).thenReturn(bootstrap);
            Assertions.assertSame(context, BootstrapContext.get());
        }
    }
}
