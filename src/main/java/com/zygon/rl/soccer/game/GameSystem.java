package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.pitch.Pitch;

import java.util.function.BiConsumer;

/**
 * Taken from CoreRL
 *
 * @author zygon
 */
public abstract class GameSystem implements BiConsumer<Game, Pitch> {

    private final GameConfiguration gameConfiguration;

    protected GameSystem(GameConfiguration gameConfiguration) {
        this.gameConfiguration = gameConfiguration;
    }

    protected final GameConfiguration getGameConfiguration() {
        return gameConfiguration;
    }
}
