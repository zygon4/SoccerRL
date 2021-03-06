package com.zygon.rl.soccer.core;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public final class Location {

    private final int x;
    private final int y;
    private final Identifier identifier;

    public Location(int x, int y) {
        this.x = x;
        this.y = y;
        this.identifier = new Identifier(this.x, this.y);
    }

    private Location(Identifier id) {
        this(id.getCoordinates()[0], id.getCoordinates()[1]);
    }

    public double getDistance(Location o) {
        return identifier.getDistance(o.identifier);
    }

    public List<Location> getPath(Location o) {
        List<Identifier> path = this.identifier.getPath(o.identifier);

        return path.stream()
                .map(id -> new Location(id))
                .collect(Collectors.toList());
    }

    public Collection<Location> getRadius(Location o, long radius) {
        return this.identifier.getNeighbors(radius).stream()
                .map(id -> new Location(id))
                .collect(Collectors.toList());
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Location setX(int x) {
        return new Location(x, this.y);
    }

    public Location setY(int y) {
        return new Location(this.x, y);
    }

    @Override
    public final String toString() {
        return x + "/" + y;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Location)) {
            return false;
        }

        return this.hashCode() == ((Location) obj).hashCode();
    }

    @Override
    public int hashCode() {
        // Not ideal
        return toString().hashCode();
    }
}
