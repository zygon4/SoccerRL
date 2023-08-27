package com.zygon.rl.soccer.core;

import java.util.UUID;

/**
 *
 * @author zygon
 */
public abstract class Action {

    private final UUID uuid;

    protected Action() {
        this.uuid = UUID.randomUUID();
    }

    public final UUID getUuid() {
        return uuid;
    }

    public abstract String getDisplayString();
}
