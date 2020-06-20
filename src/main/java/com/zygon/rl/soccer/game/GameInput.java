package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.utils.Pair;

import java.util.Map;

/**
 *
 * @author zygon
 */
public class GameInput {

    // TODO: support generic non-player actions like "check stats", etc.
    private final Map<Player, Pair<PlayerAction, Location>> actionsByPlayer;

    public GameInput(Map<Player, Pair<PlayerAction, Location>> actionsByPlayer) {
        this.actionsByPlayer = actionsByPlayer;
    }

    public Map<Player, Pair<PlayerAction, Location>> getActionsByPlayer() {
        return actionsByPlayer;
    }

    String toDisplayString(Game game) {
        StringBuilder sb = new StringBuilder();

        Team home = game.getHomeTeam();
        Team away = game.getAwayTeam();

        for (Map.Entry<Player, Pair<PlayerAction, Location>> entry : actionsByPlayer.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getLeft())
                    .append(" ")
                    .append(entry.getValue().getRight().getX())
                    .append("/")
                    .append(entry.getValue().getRight().getY());

            sb.append("\n");
        }

        return sb.toString();
    }
}
