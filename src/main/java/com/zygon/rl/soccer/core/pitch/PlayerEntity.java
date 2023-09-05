package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;

/**
 * TODO: make immutable or at least more safe.
 */
public class PlayerEntity extends PitchEntity {

    private final Player player;
    private Location destination;
    private boolean highlighted;

    public PlayerEntity(Player player) {
        // Using toString() is "okay" here
        super(player.toString());
        this.player = player;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getDestination() {
        return destination;
    }

    public boolean hasDestination() {
        return getDestination() != null;
    }

    void setDestination(Location destination) {
        this.destination = destination;
    }

    public boolean isHighlighted() {
        return highlighted;
    }

    void setHighlighted(boolean highlighted) {
        this.highlighted = highlighted;
    }
}
