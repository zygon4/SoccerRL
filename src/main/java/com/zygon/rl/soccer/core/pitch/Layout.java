package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.SoccerTile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class Layout {

    private final Map<Location, SoccerTile> pitch;

    // TODO: other areas such as penalty areas.
    private final Set<Location> halfAGoals;
    private final Set<Location> halfBGoals;

    private Layout(Map<Location, SoccerTile> pitch, Set<Location> halfAGoals,
            Set<Location> halfBGoals) {
        this.pitch = Collections.unmodifiableMap(pitch);
        this.halfAGoals = Collections.unmodifiableSet(halfAGoals);
        this.halfBGoals = Collections.unmodifiableSet(halfBGoals);
    }

    // https://en.wikipedia.org/wiki/Football_pitch#/media/File:Soccer_pitch_dimensions.png
    public static Layout constructPitch(
            int fieldWidth,
            int fieldLength,
            int pitchWidth,
            int pitchHeight,
            int gameFieldXOffset,
            int gameFieldYOffset) {

        Map<Location, SoccerTile> pitch = new HashMap<>();

        // Full field
        pitch.putAll(drawSquare(0, fieldWidth, 0, fieldLength, SoccerTile.GRASS, false));
        // Pitch itself
        pitch.putAll(drawSquare(gameFieldXOffset, pitchWidth, gameFieldYOffset, pitchHeight, SoccerTile.PITCH, false));
        // Outline
        pitch.putAll(drawSquare(gameFieldXOffset - 1, pitchWidth + 2, gameFieldYOffset - 1, pitchHeight + 2, SoccerTile.LINE, true));
        // Center line
        pitch.putAll(drawSquare(gameFieldXOffset - 1, pitchWidth + 2, gameFieldYOffset - 1, (pitchHeight + 2) / 2, SoccerTile.LINE, true));

        // Penalty squares
        float penaltyAreaDistanceFromEdgeRatio = .20f;
        int pentaltyAreaXOffset = Math.round(pitchWidth * penaltyAreaDistanceFromEdgeRatio);
        float penaltyAreaDistanceFromGoalRatio = 0.10f;
        int penaltyAreaYOffset = Math.round(pitchHeight * penaltyAreaDistanceFromGoalRatio);
        pitch.putAll(drawSquare(gameFieldXOffset + pentaltyAreaXOffset, pitchWidth - pentaltyAreaXOffset * 2,
                gameFieldYOffset - 1, penaltyAreaYOffset,
                SoccerTile.LINE, true));
        pitch.putAll(drawSquare(gameFieldXOffset + pentaltyAreaXOffset, pitchWidth - pentaltyAreaXOffset * 2,
                fieldLength - gameFieldYOffset - penaltyAreaYOffset + 1, penaltyAreaYOffset,
                SoccerTile.LINE, true));

        // Goal keeper area
        pitch.putAll(drawSquare(gameFieldXOffset + (pitchWidth / 2) - 6, 12, gameFieldYOffset - 1, 3, SoccerTile.LINE, true));
        pitch.putAll(drawSquare(gameFieldXOffset + (pitchWidth / 2) - 6, 12, gameFieldYOffset + pitchHeight - 2, 3, SoccerTile.LINE, true));

        // Goals
        Map<Location, SoccerTile> halfAGoalTiles = drawSquare(gameFieldXOffset + (pitchWidth / 2) - 4, 8, gameFieldYOffset - 2, 2, SoccerTile.GOAL, false);
        Set<Location> halfAGoals = halfAGoalTiles.keySet();
        pitch.putAll(halfAGoalTiles);

        Map<Location, SoccerTile> halfBGoalTiles = drawSquare(gameFieldXOffset + (pitchWidth / 2) - 4, 8, gameFieldYOffset + pitchHeight, 2, SoccerTile.GOAL, false);
        Set<Location> halfBGoals = halfBGoalTiles.keySet();
        pitch.putAll(halfBGoalTiles);

        // Center circle
        // TODO: just outline
//        Location center = Location.create(
//                gameFieldXOffset + pitchWidth / 2,
//                gameFieldYOffset - 1 + (pitchHeight / 2));
//        center.getNeighbors(5).forEach(l -> pitch.put(l, SoccerTile.LINE));
        return new Layout(pitch, halfAGoals, halfBGoals);
    }

    private static Map<Location, SoccerTile> drawSquare(int startX, int xLength,
            int startY, int yLength, SoccerTile tile, boolean outline) {
        Map<Location, SoccerTile> pitch = new HashMap<>();
        for (int y = startY; y < startY + yLength; y++) {
            for (int x = startX; x < startX + xLength; x++) {
                if (!outline) {
                    pitch.put(Location.create(x, y), tile);
                } else {
                    if (x == startX || x == (startX + xLength - 1)
                            || y == startY || y == (startY + yLength - 1)) {
                        pitch.put(Location.create(x, y), tile);
                    }
                }
            }
        }
        return pitch;
    }

    public Set<Location> getHalfAGoals() {
        return halfAGoals;
    }

    public Set<Location> getHalfBGoals() {
        return halfBGoals;
    }

    public SoccerTile getTile(Location location) {
        return this.pitch.get(location);
    }

    // just for testing here..
    private static String getChar(SoccerTile tile) {
        switch (tile) {
            case GRASS:
                return "~";
            case PITCH:
                return ".";
            case BALL:
                return "+";
            case GOAL:
                return "=";
        }
        return "?";
    }

    public static void main(String[] args) {
        Layout layout = constructPitch(
                Pitch.FIELD_WIDTH, Pitch.FIELD_HEIGHT,
                Pitch.PITCH_WIDTH, Pitch.PITCH_HEIGHT,
                Pitch.PITCH_FIELD_WIDTH_OFFSET, Pitch.PITCH_FIELD_HEIGHT_OFFSET);

        for (int y = 0; y < Pitch.FIELD_HEIGHT; y++) {
            for (int x = 0; x < Pitch.FIELD_WIDTH; x++) {
                System.out.print(getChar(layout.getTile(Location.create(x, y))));
            }
            System.out.println(" " + y);
        }
    }
}
