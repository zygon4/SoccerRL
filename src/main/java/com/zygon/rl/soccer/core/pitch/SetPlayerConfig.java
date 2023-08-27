package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;

import java.util.Objects;

/**
 * Moving an entity anywhere from anywhere as long as the original space has
 * something and the destination is empty.
 */
public class SetPlayerConfig extends Pitch.PitchAction {

    private final PlayerEntity player;
    private final Location destination;
    private final String displayString;

    public SetPlayerConfig(PlayerEntity player, Location destination) {
        this.player = Objects.requireNonNull(player);
        this.destination = destination;
        this.displayString = "SET DEST " + this.player + " TO " + this.destination;
    }

    @Override
    public boolean canExecute(Pitch pitch) {
        return true;
    }

    @Override
    public void doExecute(Pitch pitch) {
        if (!canExecute(pitch)) {
            throw new IllegalStateException(getDisplayString());
        }
        player.setDestination(destination);
        pitch.getPitchEntites().save(player, pitch.getLocation(player));
    }

    @Override
    public String getDisplayString() {
        return displayString;
    }
}
