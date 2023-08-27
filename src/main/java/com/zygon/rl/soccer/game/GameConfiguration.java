package com.zygon.rl.soccer.game;

import java.util.Random;

/**
 * Starting off as a POJO for now in case we need to JSON it.
 */
public class GameConfiguration {

    private Random random;

    public Random getRandom() {
        return random;
    }

    public void setRandom(Random random) {
        this.random = random;
    }
}
