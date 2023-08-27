package com.zygon.rl.soccer.core;

import java.util.Objects;

/**
 *
 * @author zygon
 */
@Deprecated // For PlayerEntity
public class PlayerGameStatus {

    // TODO: this class needs a lot more fun features.
    private final Player player;
    private final Location destination;
    private final int condition;
    // TODO: yellow cards
    // TODO: time of possession

    // Maybe builder time?
    private PlayerGameStatus(Player player, Location destination, int condition) {
        this.player = Objects.requireNonNull(player);
        this.destination = destination;
        this.condition = condition;
    }

    public PlayerGameStatus(Player player) {
        this(player, null, 100);
    }

    public Player getPlayer() {
        return player;
    }

    public Location getDestination() {
        return destination;
    }

    public int getCondition() {
        return condition;
    }

    public PlayerGameStatus setDestination(Location destination) {
        return new PlayerGameStatus(player, destination, condition);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getPlayer())
                .append("\n")
                .append(getDestination())
                .append("\n")
                .append(getCondition());

        return sb.toString();
    }
}
