package com.zygon.rl.soccer.core;

/**
 * Don't lie.
 */
public record Ball(int xyAngle, int zAngle) {

    public static Ball create(int xyAngle, int zAngle) {
        return new Ball(xyAngle, zAngle);
    }

    public boolean isOnGround() {
        return true;
    }
}
