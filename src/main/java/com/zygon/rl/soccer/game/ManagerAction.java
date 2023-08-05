package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.Formation;

import java.util.Objects;

/**
 *
 * @author zygon
 */
@Deprecated
public class ManagerAction extends Action {

    public enum Action {
        CANCEL, // generic "go back, clear"
        MANAGER_PLAYER_STATS,
        MANAGER_TEAM_FORMATION;

        // TODO: EXIT/FORFIT?
    }

    private final ManagerAction.Action action;
    private final Formation formation;

    private ManagerAction(Action action, Formation formation) {
        this.action = Objects.requireNonNull(action);
        this.formation = formation;
    }

    public static ManagerAction create(Action action) {
        return new ManagerAction(action, null);
    }

    public static ManagerAction setFormation(Formation formation) {
        return new ManagerAction(Action.MANAGER_TEAM_FORMATION, formation);
    }

    public Action getAction() {
        return action;
    }

    public Formation getFormation() {
        return formation;
    }

    @Override
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();

        if (getAction() == Action.MANAGER_TEAM_FORMATION) {
            sb.append("Set formation to ").append(getFormation().getName());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
