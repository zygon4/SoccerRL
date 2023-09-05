package com.zygon.rl.soccer.core.pitch;

import java.util.Objects;

/**
 *
 */
public class ApplyBallPressure extends Pitch.PitchAction {

    private final PitchBall ball;
    private final int force;
    private final int angle;
    private final String displayString;

    public ApplyBallPressure(PitchBall ball, int force, int angle) {
        this.ball = Objects.requireNonNull(ball);
        this.force = force;
        this.angle = angle;
        this.displayString = "SET FORCE " + this.force + ", ANGLE " + this.angle + " TO " + this.ball;
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
        ball.applyForce(force, angle);
        pitch.save(ball, pitch.getBallLocation());
    }

    @Override
    public String getDisplayString() {
        return displayString;
    }
}
