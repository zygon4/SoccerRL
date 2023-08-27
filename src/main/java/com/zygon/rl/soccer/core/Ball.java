package com.zygon.rl.soccer.core;

/**
 * Don't lie.
 */
public record Ball(int xyAngle, int zAngle, double weight) {

    public static Ball create(int xyAngle, int zAngle, double weight) {
        return new Ball(xyAngle, zAngle, weight);
    }

    public boolean isOnGround() {
        return true;
    }
}
