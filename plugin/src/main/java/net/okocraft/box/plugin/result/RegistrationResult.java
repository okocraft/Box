package net.okocraft.box.plugin.result;

import net.okocraft.box.api.util.Result;

public enum RegistrationResult implements Result {
    SUCCESS,
    ALREADY_REGISTERED,
    EXCEPTION_OCCURS
    ;

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
