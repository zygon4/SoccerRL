package com.zygon.rl.soccer.core;

import java.util.Optional;

/**
 *
 * @author zygon
 */
public final class LocationItems {

    private boolean hasBall = false;
    private Player player = null;

    void setHasBall(boolean hasBall) {
        this.hasBall = hasBall;
    }

    public boolean hasBall() {
        return hasBall;
    }

    void setPlayer(Player player) {
        this.player = player;
    }

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(player);
    }

}
