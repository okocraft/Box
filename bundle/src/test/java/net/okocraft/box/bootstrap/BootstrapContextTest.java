package net.okocraft.box.bootstrap;

import net.okocraft.box.api.bootstrap.BootstrapContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class BootstrapContextTest {
    @Test
    void testGet() {
        try (MockedStatic<BoxBootstrap> mock = Mockito.mockStatic(BoxBootstrap.class)) {
            BoxBootstrap bootstrap = Mockito.mock(BoxBootstrap.class);
            BoxBootstrapContext context = Mockito.mock(BoxBootstrapContext.class);
            Mockito.when(bootstrap.getContext()).thenReturn(context);
            mock.when(BoxBootstrap::get).thenReturn(bootstrap);
            Assertions.assertSame(context, BootstrapContext.get());
        }
    }
}
