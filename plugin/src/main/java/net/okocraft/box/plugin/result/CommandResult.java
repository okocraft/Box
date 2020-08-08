package net.okocraft.box.plugin.result;

import net.okocraft.box.api.util.Result;

public enum CommandResult implements Result {
    EXCEPTION_OCCURRED,
    INVALID_ARGUMENTS,
    NOT_PLAYER,
    NO_ARGUMENT,
    NO_PERMISSION,
    STATE_ERROR,
    SUCCESS;

    @Override
    public boolean isSuccess() {
        return this == SUCCESS;
    }
}
