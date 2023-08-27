package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Ball;

import java.util.Objects;

/**
 *
 */
public class PitchBall extends PitchEntity {

    private final Ball ball;
    // simple concept of "force equals distance" until physics can be added.
    private int force; // acceleration?
    private int angle;

    public PitchBall(Ball ball) {
        super("ball");
        this.ball = Objects.requireNonNull(ball);
    }

    @Override
    public boolean isBall() {
        return true;
    }

    public Ball getBall() {
        return ball;
    }

    public int getForce() {
        return force;
    }

    public int getAngle() {
        return angle;
    }

    // Would prefer immutable
    void applyForce(int force, int angle) {
        this.force = force;
        this.angle = angle;
    }

    public void applyForce(int force) {
        applyForce(force, angle);
    }
}
