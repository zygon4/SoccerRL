package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Action;

import java.util.Objects;

/**
 *
 * @author zygon
 */
@Deprecated
public class HumanAction extends Action {

    public static final HumanAction PLAY_VS_PC = new HumanAction(Action.PLAY_VS_PC);
    public static final HumanAction QUIT = new HumanAction(Action.QUIT);

    public static enum Action {
        PLAY_VS_PC,
        QUIT
    }

    private final Action action;

    private HumanAction(Action action) {
        this.action = Objects.requireNonNull(action);
    }

    public Action getAction() {
        return action;
    }

    @Override
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();
        sb.append(action.name());
        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
