package com.zygon.rl.soccer.core;

import java.util.Objects;

/**
 * Static player info.
 */
public class Player {

    private final int number;
    private final Team team;
    private final double speed;
    private final double power;
    private final double reach;
    private final double finesse; // ? this is fuzzy, maybe act like tiebreaker?

    public Player(int number, Team team, double speed, double power,
            double reach, double finesse) {
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
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.number;
        hash = 97 * hash + Objects.hashCode(this.team);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Player other = (Player) obj;
        if (this.number != other.number) {
            return false;
        }
        if (!Objects.equals(this.team.getName(), other.team.getName())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getNumber() + "[" + team.getName() + "]";
    }
}
