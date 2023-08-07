package com.zygon.rl.soccer.core;

import java.util.List;

/**
 *
 * @author zygon
 */
public interface Formation {

    String getName();

    /**
     * Returns a list of groups starting in the backfield e.g. 4-4-2
     *
     * @return a list of groups starting in the backfield
     */
    List<Integer> getPlayerCountAtZone();

    /**
     * Returns the number of 'zones' where players will line up. e.g. 4-4-2 = 3
     *
     * @return the number of 'zones' where players will line up.
     */
    int getZoneCount();
}
