package com.zygon.rl.soccer.utils;

/**
 *
 * @author zygon
 */
public class Pair<L, R> {

    private final L left;
    private final R right;

    private Pair(L left, R right) {
        this.left = left;
        this.right = right;
    }

    public static <L, R> Pair<L, R> create(L left, R right) {
        return new Pair(left, right);
    }

    public L getLeft() {
        return left;
    }

    public R getRight() {
        return right;
    }
}
