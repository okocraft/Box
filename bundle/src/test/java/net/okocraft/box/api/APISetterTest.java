package net.okocraft.box.api;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class APISetterTest {

    @Test
    void testSetAndUnset() {
        BoxAPI api = Mockito.mock(BoxAPI.class);

        APISetter.set(api);

        Assertions.assertSame(api, BoxAPI.api());
        Assertions.assertSame(api, BoxProvider.API);
        Assertions.assertSame(api, BoxProvider.get());

        APISetter.unset();

        Assertions.assertThrows(IllegalStateException.class, BoxAPI::api);
        Assertions.assertNull(BoxProvider.API);
        Assertions.assertThrows(IllegalStateException.class, BoxProvider::get);
    }

    @Test
    void testIllegalState() {
        Assertions.assertThrows(IllegalStateException.class, APISetter::unset); // unset when no API is set

        BoxAPI api = Mockito.mock(BoxAPI.class);

        APISetter.set(api);

        Assertions.assertThrows(IllegalStateException.class, () -> APISetter.set(api)); // duplicated API setting

        APISetter.unset();

        Assertions.assertThrows(IllegalStateException.class, APISetter::unset); // unset when no API is set
    }
}
