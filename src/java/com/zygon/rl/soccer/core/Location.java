package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
public final class Location {

    private final int x;
    private final int y;
    // Mutable field, I hate mutation but in games performance is a thing
    private LocationItems locationItems = null;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public LocationItems getLocationItems() {
        return locationItems;
    }

    void setLocationItems(LocationItems locationItems) {
        this.locationItems = locationItems;
    }
}
