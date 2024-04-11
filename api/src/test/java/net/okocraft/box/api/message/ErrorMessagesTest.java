package net.okocraft.box.api.message;

import org.junit.jupiter.api.Test;

class ErrorMessagesTest {

    @Test
    void testInitialize() {
        // Checks if the fields can be accessed successfully without exceptions.
        ErrorMessages.NO_PERMISSION.apply("");
    }

}
