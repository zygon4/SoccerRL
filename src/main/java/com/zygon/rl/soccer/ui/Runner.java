package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.game.GameConfiguration;
import com.zygon.rl.soccer.game.GameImpl;

import java.util.Random;

/**
 * Runner class.
 */
public class Runner {

    /**
     * Main entry point for the game.
     *
     * @param args none needed
     */
    public static void main(String[] args) {

        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            e.printStackTrace(System.err);
        });

        GameConfiguration config = new GameConfiguration();
        config.setRandom(new Random());
        UserInterface ui = new UserInterface(new GameImpl(config));
        ui.start();
    }
}
