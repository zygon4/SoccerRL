package com.zygon.rl.soccer.core;

import com.zygon.rl.data_structures.AStar;
import com.zygon.rl.data_structures.Graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class Identifier implements Comparable<Identifier> {

    private final int[] coordinates;
    private final int dimensions;
    private final String display;

    private int hash = -1;

    public Location toLocation() {
        return new Location(coordinates[0], coordinates[1]);
    }

    public Identifier(int... coords) {
        Objects.requireNonNull(coords);
        if (coords.length <= 0) {
            throw new IllegalArgumentException();
        }

        this.coordinates = coords;
        this.dimensions = this.coordinates.length;

        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < this.coordinates.length; i++) {
            sb.append(this.coordinates[i]);
            if (i < this.coordinates.length - 1) {
                sb.append("_");
            }
        }

        this.display = sb.toString();
    }

    // I don't like exposing this
    public final int getDimensions() {
        return dimensions;
    }

    /* Distances by dimension */
    public int[] getDistances(Identifier o) {

        if (o.dimensions != this.dimensions) {
            throw new IllegalArgumentException();
        }

        int[] distancesByDimension = new int[this.dimensions];

        for (int i = 0; i < this.coordinates.length; i++) {
            distancesByDimension[i] = o.coordinates[i] - this.coordinates[i];
        }

        return distancesByDimension;
    }

    /**
     * Returns an array of angles between these two Identifiers.
     *
     * @param o
     * @return
     */
    public double getDirection(Identifier o) {
        if (o.dimensions != this.dimensions) {
            throw new IllegalArgumentException();
        }

        // right now this will support 2D - 3D to come
        if (o.dimensions != 2) {
            throw new IllegalArgumentException();
        }

        double[] pointLocationDeltas = new double[this.dimensions];

        for (int i = 0; i < this.dimensions; i++) {
            pointLocationDeltas[i] = o.coordinates[i] - this.coordinates[i];
        }

        return Math.toDegrees(Math.atan2(pointLocationDeltas[1], pointLocationDeltas[0]));
    }

    public static void main(String[] args) {
        System.out.println(Math.toDegrees(Math.atan2(1, 1)));
        System.out.println(Math.toDegrees(Math.atan2(-1, 1)));
        System.out.println(Math.toDegrees(Math.atan2(1, -1)));
        System.out.println(Math.toDegrees(Math.atan2(-1, -1)));

        System.out.println("--------------------------");

        System.out.println(Math.toDegrees(Math.atan2(1, 0)));
        System.out.println(Math.toDegrees(Math.atan2(-1, 0)));
        System.out.println(Math.toDegrees(Math.atan2(0, 1)));
        System.out.println(Math.toDegrees(Math.atan2(0, -1)));

        System.out.println("--------------------------");

        double[] angles = {
            new Identifier(0, 0).getDirection(new Identifier(-1, 1)),
            new Identifier(0, 0).getDirection(new Identifier(0, 1)),
            new Identifier(0, 0).getDirection(new Identifier(1, 1)),
            new Identifier(0, 0).getDirection(new Identifier(-1, 0)),
            new Identifier(0, 0).getDirection(new Identifier(0, 0)),
            new Identifier(0, 0).getDirection(new Identifier(1, 0)),
            new Identifier(0, 0).getDirection(new Identifier(-1, -1)),
            new Identifier(0, 0).getDirection(new Identifier(0, -1)),
            new Identifier(0, 0).getDirection(new Identifier(1, -1)),};

        for (double angle : angles) {
            System.out.println(angle);
        }

        System.out.println("--------------------------");
        double[] angles2 = {
            new Identifier(1, 1).getDirection(new Identifier(-1, 1)),
            new Identifier(0, 0).getDirection(new Identifier(0, 1)),
            new Identifier(0, 0).getDirection(new Identifier(1, 1)),
            new Identifier(0, 0).getDirection(new Identifier(-1, 0)),
            new Identifier(0, 0).getDirection(new Identifier(0, 0)),
            new Identifier(0, 0).getDirection(new Identifier(1, 0)),
            new Identifier(0, 0).getDirection(new Identifier(-1, -1)),
            new Identifier(0, 0).getDirection(new Identifier(0, -1)),
            new Identifier(0, 0).getDirection(new Identifier(1, -1)),};

        for (double angle : angles2) {
            System.out.println(angle);
        }

        List<Identifier> path = new Identifier(0, 0).getPath(new Identifier(4, 4));
    }

    /*
    (defn- dis [x y]
        (math/sqrt (reduce + (map (fn [a b] (math/expt (- a b) 2)) x y))))
     */
    public double getDistance(Identifier o) {

        if (o.dimensions != this.dimensions) {
            throw new IllegalArgumentException();
        }

        double total = 0.0;

        for (int i = 0; i < this.coordinates.length; i++) {
            total += Math.pow(this.coordinates[i] - o.coordinates[i], 2);
        }

        return Math.sqrt(total);
    }

    // This does not seem to fully work for 3d
    // TBD: direction option
    public Collection<Identifier> getNeighbors(long radius) {
        Collection<Identifier> neighbors = new ArrayList<>();

        // For each dimension
        for (int i = 0; i < this.dimensions; i++) {
            // Take the value AT that dimension and find the neighbors based on
            // the radius.

            int dimValue = this.coordinates[i];

            long min = Math.max(dimValue - Math.round(((double) radius / 2.0)), 0);
            long max = Math.min(dimValue + Math.round((double) (radius / 2.0)), Integer.MAX_VALUE);

            // squirrely casting
            for (int j = (int) min; j <= max; j++) {
                if (j != dimValue) {
                    int[] coords = Arrays.copyOf(this.coordinates, this.coordinates.length);
                    coords[i] = j;
                    neighbors.add(new Identifier(coords));
                }
            }
        }

        return neighbors;
    }

    // Limited by 2D, manhattan path, no diags yet
    public List<Identifier> getPath(Identifier identifier) {

        // need to create a grid to encompass minX/minY - maxX/maxY
        int minX = Math.min(this.coordinates[0], identifier.coordinates[0]);
        int minY = Math.min(this.coordinates[1], identifier.coordinates[1]);
        int maxX = Math.max(this.coordinates[0], identifier.coordinates[0]);
        int maxY = Math.max(this.coordinates[1], identifier.coordinates[1]);

        Set<Graph.Vertex<Identifier>> verticies = new HashSet<>();
        Set<Graph.Edge<Identifier>> edges = new HashSet<>();

        // Keep track of verticies by Id
        Map<Identifier, Graph.Vertex<Identifier>> ids = new HashMap<>();

        // Generate edges and verticies
        for (int x = minX; x < maxX + 1; x++) {
            for (int y = minY; y < maxY + 1; y++) {
                Identifier id = new Identifier(x, y);

                Graph.Vertex<Identifier> vertex = new Graph.Vertex<>(id);
                ids.put(id, vertex);
                verticies.add(vertex);

                // Edges; check neighbors
                for (Identifier neighbor : id.getNeighbors(1)) {

                    // cache known vertex
                    Graph.Vertex<Identifier> neighborVert = ids.get(neighbor);
                    if (neighborVert == null) {
                        neighborVert = new Graph.Vertex<>(neighbor);
                        ids.put(neighbor, neighborVert);
                    }

                    // TODO: cost based on other players there and/or in the area
                    Graph.Edge<Identifier> neighborEdge = new Graph.Edge<>(1, vertex, neighborVert);
                    vertex.addEdge(neighborEdge);
                    if (!edges.contains(neighborEdge)) {
                        edges.add(neighborEdge);
                    }
                }
            }
        }

        Graph<Identifier> idGraph = new Graph<>(Graph.TYPE.UNDIRECTED, verticies, edges);
        AStar<Identifier> aStar = new AStar<>();
        List<Graph.Edge<Identifier>> aStarPath = aStar.aStar(
                idGraph, ids.get(this), ids.get(identifier));

        return aStarPath == null ? null : aStarPath.stream()
                .map(e -> e.getFromVertex().getValue())
                .collect(Collectors.toList());
    }

    @Override
    public int compareTo(Identifier o) {
        return o.hashCode() > this.hashCode() ? -1 : (o.hashCode() < this.hashCode() ? 1 : 0);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Identifier)) {
            return false;
        }

        return this.hashCode() == ((Identifier) obj).hashCode();
    }

    @Override
    public final int hashCode() {

        if (this.hash == -1) {
            this.hash = Arrays.hashCode(this.coordinates);
        }

        return this.hash;
    }

    public String getDisplay() {
        return this.display;
    }

    @Override
    public String toString() {
        return this.getDisplay();
    }

    /* pkg */ int[] getCoordinates() {
        return coordinates;
    }
}
