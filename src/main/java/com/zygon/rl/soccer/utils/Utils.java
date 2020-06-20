package com.zygon.rl.soccer.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Optional;

/**
 *
 * @author zygon
 */
public class Utils {

    private Utils() {
        // private ctr
    }

    public static double round(double val) {
        return Math.round(val * 100) / 100D;
    }

    public static String getStdIn(Optional<String> prompt) throws IOException {
        if (prompt.isPresent()) {
            System.out.println(prompt.get());
        }

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        return br.readLine();
    }
}
