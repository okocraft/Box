package net.okocraft.box.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class BoxAPITest {

    @Test
    void testObtainingAPI() {
        try {
            // First, checking if the BoxAPI is not set.
            Assertions.assertFalse(BoxAPI.isLoaded());
            Assertions.assertThrows(IllegalStateException.class, BoxAPI::api);

            // Set dummy BoxAPI
            BoxProvider.API = Mockito.mock(BoxAPI.class);

            // Checking if the BoxAPI can be obtained.
            Assertions.assertTrue(BoxAPI.isLoaded());
            Assertions.assertDoesNotThrow(BoxAPI::api);
            Assertions.assertSame(BoxProvider.API, BoxAPI.api());
        } finally {
            BoxProvider.API = null;
        }
    }

}
