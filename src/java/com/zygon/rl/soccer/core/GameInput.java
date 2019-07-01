package com.zygon.rl.soccer.core;

import java.util.Map;

/**
 *
 * @author zygon
 */
public class GameInput {

    // TODO: this doesn't support generic non-player actions like "check stats", etc.
    private final Map<Player, Pair<PlayerAction, Location>> actionsByPlayer;

    public GameInput(Map<Player, Pair<PlayerAction, Location>> actionsByPlayer) {
        this.actionsByPlayer = actionsByPlayer;
    }

    public Map<Player, Pair<PlayerAction, Location>> getActionsByPlayer() {
        return actionsByPlayer;
    }

    String toDisplayString(Game game) {
        StringBuilder sb = new StringBuilder();

        Team teamA = game.getTeamA();
        Team teamB = game.getTeamB();

        for (Map.Entry<Player, Pair<PlayerAction, Location>> entry : actionsByPlayer.entrySet()) {
            sb.append(entry.getKey())
                    .append(": ")
                    .append(entry.getValue().getLeft().name())
                    .append(" ")
                    .append(entry.getValue().getRight().getX())
                    .append("/")
                    .append(entry.getValue().getRight().getY());

            sb.append("\n");
        }

        return sb.toString();
    }
}
