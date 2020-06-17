package com.zygon.rl.soccer.core;

/**
 *
 * @author zygon
 */
public final class PlayerAction extends Action {

    public enum Action {

        // TODO: implement MOVE
        PASS,
        SHOOT;
    }

    private final Player player;
    private final Action action;
    private final Player teammate;
    private final Location location;

    private PlayerAction(Player player, Action action, Player teammate,
            Location location) {
        this.player = player;
        this.action = action;
        this.teammate = teammate;
        this.location = location;
    }

    // The difference between pass and shoot is subtle. Ultimately i'd like to
    // reconcile them as a pass, but each shot should use different stats/meters.
    // E.g. A short accurate PASS should use finesse and not a lot of energy,
    //      a long SHOT should take power and use more energy.
    public static PlayerAction shoot(Player player, Location location) {
        return new PlayerAction(player, Action.SHOOT, null, location);
    }

    public static PlayerAction pass(Player player, Player teammate) {
        return new PlayerAction(player, Action.PASS, teammate, null);
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

    public Location getLocation() {
        return location;
    }

    @Override
    public String getDisplayString() {
        StringBuilder sb = new StringBuilder();

        sb.append(getPlayer()).append(" ").append(getAction().name());
        if (getAction() == Action.PASS) {
            sb.append(" to ").append(getTeammate());
        }

        return sb.toString();
    }

    @Override
    public String toString() {
        return getDisplayString();
    }
}
