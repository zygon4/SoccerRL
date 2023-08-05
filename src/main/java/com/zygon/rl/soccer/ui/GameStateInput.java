/*
 *
 */
package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.ui.GameStateInput;

/**
 *
 * @author zygon
 */
public record GameStateInput(Type type, Player player, Location location) {

    public enum Type {
        LOCATION_SELECT,
        SINGLE_PLAYER_SELECT,
        CANCEL; // escape char
    }

//    private final Type type;
//    private final Player player;
//    private final Location location;
//
//    private GameStateInput(Type type, Player player, Location location) {
//        this.type = type;
//        this.player = player;
//        this.location = location;
//    }
    private static GameStateInput create(Type type, Location location) {
        return new GameStateInput(type, null, location);
    }

    public static GameStateInput cancel() {
        return new GameStateInput(Type.CANCEL, (Player) null, (Location) null);
    }

    public static GameStateInput selectPlayer(Player player, Location location) {
        return new GameStateInput(Type.SINGLE_PLAYER_SELECT, player, location);
    }

    public static GameStateInput selectLocation(Location location) {
        return create(Type.LOCATION_SELECT, location);
    }

    /*pkg*/ Type getType() {
        return type;
    }

    /*pkg*/ Player getPlayer() {
        return player;
    }

    /*pkg*/ Location getLocation() {
        return location;
    }
}
