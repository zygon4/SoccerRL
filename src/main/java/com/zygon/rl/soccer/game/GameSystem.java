package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.pitch.Pitch;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

/**
 * Taken from CoreRL
 *
 * @author zygon
 */
public abstract class GameSystem implements BiConsumer<Game, Pitch> {

    private final Executor executor = Executors.newFixedThreadPool(3);

    private final GameConfiguration gameConfiguration;

    protected GameSystem(GameConfiguration gameConfiguration) {
        this.gameConfiguration = gameConfiguration;
    }

    protected final Executor getExecutor() {
        return executor;
    }

    protected final GameConfiguration getGameConfiguration() {
        return gameConfiguration;
    }
}
