package com.zygon.rl.soccer.core;

import com.zygon.rl.soccer.core.Ball;

import java.util.Optional;

/**
 *
 * @author zygon
 */
public final class LocationItems {

    private Ball ball = null;
    private PlayerGameStatus playerGameStatus = null;

    public Optional<Player> getPlayer() {
        return Optional.ofNullable(playerGameStatus != null
                ? playerGameStatus.getPlayer() : null);
    }

    public Optional<PlayerGameStatus> getPlayerGameStatus() {
        return Optional.ofNullable(playerGameStatus);
    }

    public boolean hasBall() {
        return ball != null;
    }

    public Ball getBall() {
        return ball;
    }

    // TODO: finish adding a first-class ball feature
    void setBall(Ball ball) {
        this.ball = ball;
    }

    void setPlayerGameStatus(PlayerGameStatus playerGameStatus) {
        if (playerGameStatus != null && this.playerGameStatus != null
                && !playerGameStatus.getPlayer().equals(this.playerGameStatus.getPlayer())) {
            throw new IllegalStateException("Player already set");
        }
        this.playerGameStatus = playerGameStatus;
    }
}
