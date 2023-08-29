package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;

import java.util.Objects;

/**
 * Moving an entity anywhere from anywhere as long as the original space has
 * something and the destination is empty.
 */
public class MovePitchEntity extends Pitch.PitchAction {

    private final Location dest;
    private final PlayerEntity srcPlayer;
    private final PitchBall srcBall;
    private final String displayString;

    public MovePitchEntity(Location dest, PlayerEntity srcPlayer) {
        this.dest = dest;
        this.srcPlayer = Objects.requireNonNull(srcPlayer);
        this.srcBall = null;
        this.displayString = "MOVE " + this.srcPlayer + " TO " + this.dest;
    }

    public MovePitchEntity(Location dest, PitchBall srcBall) {
        this.dest = dest;
        this.srcPlayer = null;
        this.srcBall = Objects.requireNonNull(srcBall);
        this.displayString = "MOVE " + this.srcBall + " TO " + this.dest;
    }

    @Override
    public boolean canExecute(Pitch pitch) {
        return Pitch.validateLegalLocation(dest)
                && (srcPlayer != null && pitch.getPlayer(dest) == null)
                || srcBall != null;
    }

    @Override
    public void doExecute(Pitch pitch) {
        if (canExecute(pitch)) {
            if (srcPlayer != null) {
                Location srcLoc = pitch.getLocation(srcPlayer);
                pitch.getPitchEntites().delete(srcPlayer, srcLoc);
                pitch.getPitchEntites().save(srcPlayer, this.dest);
            } else {
                pitch.getPitchEntites().delete(srcBall, pitch.getBallLocation());
                pitch.getPitchEntites().save(srcBall, this.dest);
            }
        }
    }

    @Override
    public String getDisplayString() {
        return displayString;
    }
}
