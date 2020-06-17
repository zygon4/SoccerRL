package com.zygon.rl.soccer.core;

import java.util.List;

/**
 *
 * @author zygon
 */
public interface Formation {

    String getName();

    // 4-2-2 = 3, 4-2-1-1 = 4
    List<Integer> getPlayerCountAtZone();

    int getZoneCount();
}
