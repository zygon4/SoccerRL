package com.zygon.rl.soccer.core;

/**
 *
 */
public class BallAction extends Action {

    private final Location location;

    private BallAction(Location location) {
        super(null);
        this.location = location;
    }

    //  TODO more fields
    public static BallAction move(Location location) {
        return new BallAction(location);
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Move to ").append(getLocation());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
