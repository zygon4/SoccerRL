package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
public class PlayerAction {

    public enum Action {

        // TODO: implement MOVE
        PASS,
        SHOOT;
    }

    private final Player player;
    private final Action action;
    private final Player teammate;

    private PlayerAction(Player player, Action action, Player teammate) {
        this.player = player;
        this.action = action;
        this.teammate = teammate;
    }

    public static PlayerAction shoot(Player player) {
        return new PlayerAction(player, Action.SHOOT, null);
    }

    public static PlayerAction pass(Player player, Player teammate) {
        return new PlayerAction(player, Action.PASS, teammate);
    }

    public Action getAction() {
        return action;
    }

    public Player getPlayer() {
        return player;
    }

    public Player getTeammate() {
        return teammate;
    }

    public String toDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getPlayer()).append(" ").append(getAction().name());
        if (getAction() == Action.PASS) {
            sb.append(" to ").append(getTeammate());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return toDisplayString();
    }
}
