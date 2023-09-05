package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;

/**
 *
 * @author zygon
 */
public class AddPitchEntity extends Pitch.PitchAction {

    private final Location location;
    private final PitchEntity pitchEntity;

    public AddPitchEntity(Location location, PitchEntity pitchEntity) {
        this.location = location;
        this.pitchEntity = pitchEntity;
    }

    @Override
    public boolean canExecute(Pitch pitch) {
        return Pitch.validateLegalLocation(location)
                && pitch.getPlayer(location) == null || pitchEntity.isBall();
    }

    @Override
    public void doExecute(Pitch pitch) {
        if (canExecute(pitch)) {
            pitch.save(pitchEntity, location);
        }
    }

    @Override
    public String getDisplayString() {
        return "ADD " + pitchEntity.getId() + " TO " + location;
    }
}
