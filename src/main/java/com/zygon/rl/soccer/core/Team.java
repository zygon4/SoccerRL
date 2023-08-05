package com.zygon.rl.soccer.core;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class Team {

    private final String name;
    private final Color color;
    private final Map<Integer, Player> players = new HashMap<>();

    public Team(String name, Color color) {
        this.name = name;
        this.color = color;
    }

    public void add(Player player) {
        this.players.put(player.getNumber(), player);
    }

    public Color getColor() {
        return color;
    }

    public String getName() {
        return name;
    }

    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    public Set<Player> getTeammates(Player player) {
        if (!hasPlayer(player.getNumber())) {
            throw new IllegalStateException("team doesnt have player " + player.toString());
        }

        return getPlayers().stream()
                .filter(p -> !p.equals(player))
                .collect(Collectors.toSet());
    }

    public boolean hasPlayer(int player) {
        return players.containsKey(player);
    }
}
