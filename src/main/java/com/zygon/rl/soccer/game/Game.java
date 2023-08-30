/*
 *
 */
package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.ui.UIAction;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Model and Controller in a rough MVC style.
 *
 * @author zygon
 */
public interface Game {

    enum State {
        PRE, // chose teams, players
        STARTED, // game is going
        POST // see results
    }

    enum TileItem {
        BALL, // ball tile (ideally not on top of the player
        DEFAULT, // grass tile
        PLAYER, // player tile
        BALL_FLIGHT, // where's the ball going
        GOAL, // A goal tile
        PLAYER_HIGHLIGHT, // hightlighting
        PLAYER_TRACK    // where's the player going
    }

    State getState();

    void start();

    void apply(UIAction action);

    void apply(PlayerAction action);

    /**
     * Returns the available actions for the group of players selected. Most
     * often expected to be a single player.
     *
     * @param players any number of players
     * @return a map of actions to their required argument type.
     */
    Map<PlayerAction.Action, PlayerAction.Argument>
            getAvailablePlayerActions(Collection<Player> players);

    /**
     * Returns the changes in the pitch since the last call.
     *
     * @return the changes in the pitch since the last call.
     */
    Map<Location, Set<TileItem>> getPitchUpdates();

    /**
     * Returns a player for the player at the specified location. Null, if no
     * player is there.
     *
     * @param location
     * @return
     */
    PlayerEntity getPlayer(Location location);

    Map<PlayerEntity, Location> getPlayers();

    Map<Team, Score> getScores();

    // Wish this wasn't here..
    Location getBall();

    // Wish this wasn't here..
    boolean isGoal(Location location);

    // primary means of watching the game progress
    void play();
}
