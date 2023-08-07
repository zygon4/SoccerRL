package com.zygon.rl.soccer.strategy;

import com.zygon.rl.soccer.core.Formation;
import com.zygon.rl.soccer.core.Location;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Performs the zone calculations. TODO: add some randomness.
 */
public class FormationHelper {

    private final Formation formation;
    private final int pitchHeight;
    private final int pitchWidth;

    public FormationHelper(Formation formation, int pitchHeight, int pitchWidth) {
        this.formation = formation;
        this.pitchHeight = pitchHeight;
        this.pitchWidth = pitchWidth;
    }

    public Set<Location> getPlayerPitchLocations(final int startingHeight) {

        final int groupHeightOffset = startingHeight / (formation.getZoneCount() + 1);

        Set<Location> locations = new LinkedHashSet<>();

        int y = groupHeightOffset;
        for (int yCount = 0; yCount < formation.getZoneCount(); yCount++) {
            // set players on x
            final int xGroupCount = formation.getPlayerCountAtZone().get(yCount);
            final int groupWidthOffset = pitchWidth / (xGroupCount + 1);

            int x = groupWidthOffset;
            for (int xCount = 0; xCount < xGroupCount; xCount++) {
                locations.add(Location.create(x, y));
                x += groupWidthOffset;
            }

            y += groupHeightOffset;
        }

        return locations;
    }
}
