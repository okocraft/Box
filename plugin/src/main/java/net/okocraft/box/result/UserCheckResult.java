package net.okocraft.box.result;

import net.okocraft.box.api.util.Result;

public enum UserCheckResult implements Result {
    NONE,
    NEW_PLAYER,
    RENAMED,
    EXCEPTION_OCCURS
    ;

    @Override
    public boolean isSuccess() {
        return this != EXCEPTION_OCCURS;
    }
}
