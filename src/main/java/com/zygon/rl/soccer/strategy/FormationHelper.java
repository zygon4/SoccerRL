package com.zygon.rl.soccer.strategy;

import com.zygon.rl.soccer.core.Formation;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Performs the zone calculations
 */
public class FormationHelper {

    private final Formation formation;
    private final List<Integer> playerCountAtZone;
    private final int totalPlayers; // not including keeper
    private final int pitchHeight;
    private final int pitchWidth;
    private final Random rand = new Random();

    public FormationHelper(Formation formation, int pitchHeight, int pitchWidth) {
        this.formation = formation;
        this.playerCountAtZone = this.formation.getPlayerCountAtZone();
        this.pitchHeight = pitchHeight;
        this.pitchWidth = pitchWidth;
        this.totalPlayers = (int) playerCountAtZone.stream().mapToInt(Integer::intValue).sum();
    }

    // this is absolute height
    public int getHeight(int zone) {
        double pctOfHeight = (double) playerCountAtZone.get(zone) / (double) totalPlayers;

        return (int) Math.round(pitchHeight * pctOfHeight);
    }

    // Returns a set of ints that represent randomly spaced points on the X (width) axes
    public Set<Integer> getRandomWidths(int count) {
        int partitionSize = pitchWidth / count;
        // every foo steps, get a random

        Set<Integer> foo = new LinkedHashSet<>();

        for (int c = 0; c < count; c++) {
            int randFoo = rand.nextInt(partitionSize);
            foo.add((c * partitionSize) + randFoo);
        }

        return foo;
    }

//    public static void main(String[] args) {
//        FormationHelper helper = new FormationHelper(Formations._4_4_1_1, 30, 20);
//        helper.getHeight(0);
//        helper.getHeight(1);
//        helper.getHeight(2);
//        helper.getHeight(3);
//
//        helper.getRandomWidths(1);
//        helper.getRandomWidths(2);
//        helper.getRandomWidths(3);
//        helper.getRandomWidths(4);
//        helper.getRandomWidths(5);
//    }
}
