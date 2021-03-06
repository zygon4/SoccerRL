package com.zygon.rl.soccer.core;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class Team {

    private final String name;
    private final Set<Player> players = new HashSet<>();

    public Team(String name) {
        this.name = name;
    }

    public void add(Player player) {
        this.players.add(player);
    }

    public String getName() {
        return name;
    }

    public Set<Player> getPlayers() {
        return players;
    }

    public Set<Player> getTeammates(Player player) {
        if (!hasPlayer(player)) {
            throw new IllegalStateException("team doesnt have player " + player.toString());
        }

        return this.players.stream()
                .filter(p -> !p.equals(player))
                .collect(Collectors.toSet());
    }

    public boolean hasPlayer(Player player) {
        return players.contains(player);
    }
}
