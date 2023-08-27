/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;

import java.util.Objects;

/**
 * Needs builder?
 *
 * @author zygon
 */
public class UIAction extends Action {

    public enum Action {
        // TODO: hightlight route, etc.
        HIGHLIGHT_PLAYER,
        HIGHLIGHT_PATH,
        UNHIGHLIGHT_PLAYER,
        SHOW_PLAYER
    }

    private final Action action;
    private final Player player;
    private final Location source;
    private final Location target;

    private UIAction(Action action, Player player, Location source,
            Location target) {
        this.action = Objects.requireNonNull(action);
        this.player = player;
        this.source = source;
        this.target = target;
    }

    public static UIAction highlightPlayer(Player player) {
        return new UIAction(Action.HIGHLIGHT_PLAYER, player, null, null);
    }

    public static UIAction highlightPath(Location start, Location target) {
        return new UIAction(Action.HIGHLIGHT_PATH, null, start, target);
    }

    public Action getAction() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }

    public Location getSource() {
        return source;
    }

    public Location getTarget() {
        return target;
    }

    @Override
    public String getDisplayString() {
        return action.name();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
