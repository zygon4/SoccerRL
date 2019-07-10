package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
class Utils {

    private Utils() {
        // private ctr
    }

    public static double round(double val) {
        return Math.round(val * 100) / 100D;
    }
}
