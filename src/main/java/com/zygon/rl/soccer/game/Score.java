package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Team;

/**
 *
 * @author zygon
 */
public class Score {

    private final Team team;
    private int SOG;
    private int goals;
    private int timeOfPossession; // turns, technically

    public Score(Team team) {
        this.team = team;
    }

    public int getGoals() {
        return goals;
    }

    public int getSOG() {
        return SOG;
    }

    public Team getTeam() {
        return team;
    }

    public int getTimeOfPossession() {
        return timeOfPossession;
    }

    public void addGoal() {
        goals++;
    }

    public void addSOG() {
        SOG++;
    }

    public void addTimeOfPossession() {
        timeOfPossession++;
    }

    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(team.getName())
                .append(" - G:")
                .append(goals)
                .append(", SOG:")
                .append(SOG)
                .append(", TOP:")
                .append(timeOfPossession);
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
