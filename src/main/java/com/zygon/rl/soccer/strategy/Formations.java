package com.zygon.rl.soccer.strategy;

import com.zygon.rl.soccer.core.Formation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author zygon
 */
public class Formations implements Formation {

    public static final Formations _4_4_2 = new Formations("4-4-2", List.of(4, 4, 2));
    public static final Formations _4_4_1_1 = new Formations("4-4-1-1", List.of(4, 4, 1, 1));
    public static final Formations _4_2_3_1 = new Formations("4-2-3-1", List.of(4, 2, 3, 1));
    public static final Formations _4_3_3 = new Formations("4-3-3", List.of(4, 3, 3));

    public static final Set<Formation> FORMATIONS = new LinkedHashSet<>();

    static {
        FORMATIONS.add(_4_4_2);
        FORMATIONS.add(_4_4_1_1);
        FORMATIONS.add(_4_3_3);
    }

    private final String name;
    private final List<Integer> playerCountAtZone;

    public Formations(String name, List<Integer> playerCountAtZone) {
        this.name = name;
        this.playerCountAtZone = Collections.unmodifiableList(playerCountAtZone);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public List<Integer> getPlayerCountAtZone() {
        return playerCountAtZone;
    }

    @Override
    public int getZoneCount() {
        return playerCountAtZone.size();
    }
}
