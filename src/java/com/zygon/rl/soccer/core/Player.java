package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
public class Player {

    private final int number;
    private final Team team;
    private final double speed;
    private final double power;
    private final double reach;
    private final double finesse; // ? this is fuzzy, maybe act like tiebreaker?

    public Player(int number, Team team, double speed, double power, double reach, double finesse) {
        this.number = number;
        this.team = team;
        this.speed = speed;
        this.power = power;
        this.reach = reach;
        this.finesse = finesse;
    }

    public int getNumber() {
        return number;
    }

    public double getFinesse() {
        return finesse;
    }

    public double getPower() {
        return power;
    }

    public double getReach() {
        return reach;
    }

    public double getSpeed() {
        return speed;
    }

    public Team getTeam() {
        return team;
    }

    @Override
    public String toString() {
        return getNumber() + "[" + team.getName() + "]";
    }
}
