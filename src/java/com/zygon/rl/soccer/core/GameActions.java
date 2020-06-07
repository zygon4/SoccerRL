package com.zygon.rl.soccer.core;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author zygon
 */
public class GameActions {

    // ManagerAction needs a refactor similar to PlayerAction
    private final Map<ManagerAction, Set<Player>> managerActions = new LinkedHashMap<>();
    private final Set<PlayerAction> playerActions = new LinkedHashSet<>();

    void add(PlayerAction playerAction) {
        playerActions.add(playerAction);
    }

    void add(ManagerAction managerAction, Player player) {
        Set<Player> players = this.managerActions.get(managerAction);
        if (players == null) {
            players = new HashSet<>();
            this.managerActions.put(managerAction, players);
        }
        // can technically have non-player specific actions
        if (player != null) {
            players.add(player);
        }
        this.managerActions.put(managerAction, players);
    }

    public Map<ManagerAction, Set<Player>> getManagerActions() {
        return Collections.unmodifiableMap(managerActions);
    }

    public Set<PlayerAction> getPlayerActions() {
        return Collections.unmodifiableSet(playerActions);
    }

    public Map<Integer, PlayerAction> getLabeledPlayerActions() {
        Map<Integer, PlayerAction> labeledActions = new LinkedHashMap<>();

        int index = 1;

        for (PlayerAction pa : playerActions) {
            labeledActions.put(index++, pa);
        }

        return labeledActions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (PlayerAction playerAction : playerActions) {
            sb.append(playerAction).append("\n");
        }

        for (Map.Entry<ManagerAction, Set<Player>> entry : managerActions.entrySet()) {
            if (entry.getValue().isEmpty()) {
                sb.append(entry.getKey().name());
            } else {
                for (Player player : entry.getValue()) {
                    sb.append(entry.getKey().name())
                            .append(": ")
                            .append(player);
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }
}
