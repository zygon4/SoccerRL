package com.zygon.rl.soccer.core;

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

    private final Map<ManagerAction, Set<Player>> managerActions = new LinkedHashMap<>();
    private final Set<Pair<Player, Player>> passFromTo = new LinkedHashSet<>();
    private Player shooter = null;

    void addPassAction(Player from, Player target) {
        // TODO: validation on from/target
        this.passFromTo.add(Pair.create(from, target));
    }

    // TODO: shooter shouldn't be required here
    void addShootAction(Player shooter) {
        this.shooter = shooter;
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
        return this.managerActions;
    }

    public Set<Pair<Player, Player>> getPassFromTo() {
        return passFromTo;
    }

    public Player getShooter() {
        return this.shooter;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Pair<Player, Player> pass : passFromTo) {
            sb.append(PlayerAction.PLAYER_PASS.name())
                    .append(": ")
                    .append(pass.getLeft())
                    .append(" TO ")
                    .append(pass.getRight())
                    .append("\n");
        }

        if (shooter != null) {
            sb.append(PlayerAction.PLAYER_SHOOT.name())
                    .append(": ")
                    .append(shooter)
                    .append("\n");
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
