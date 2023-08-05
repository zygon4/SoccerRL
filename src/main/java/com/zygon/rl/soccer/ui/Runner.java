package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.game.GameImpl;

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
        UserInterface ui = new UserInterface(new GameImpl());
        ui.start();
    }
}
