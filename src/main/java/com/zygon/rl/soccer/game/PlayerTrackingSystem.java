package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.pitch.ApplyBallPressure;
import com.zygon.rl.soccer.core.pitch.MovePitchEntity;
import com.zygon.rl.soccer.core.pitch.Pitch;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.core.pitch.SetPlayerConfig;

import java.util.List;
import java.util.Set;

/**
 * Moves players towards their destinations.
 */
public class PlayerTrackingSystem extends GameSystem {

    public PlayerTrackingSystem(GameConfiguration config) {
        super(config);
    }

    @Override
    public void accept(Game game, Pitch pitch) {

        Set<Location> playerLocations = pitch.getPlayerLocations();

        for (Location playerLocation : playerLocations) {
            PlayerEntity playerEntity = pitch.getPlayer(playerLocation);

            if (playerEntity.hasDestination() && !playerEntity.getDestination().equals(playerLocation)) {

                try {
                    if (playerLocation.equals(pitch.getBallLocation())) {
                        // Attempt to dribble the ball. We don't filter players
                        // because the ball can move into a spot with another player.
                        List<Location> ballPath = playerLocation.getPath(playerEntity.getDestination());
                        int forceAngle = playerLocation.getNeighborAngle(ballPath.get(0));

                        ApplyBallPressure dribbleKick = new ApplyBallPressure(pitch.getBall(), 3, forceAngle);
                        if (dribbleKick.canExecute(pitch)) {
                            dribbleKick.execute(pitch);
                        }
                    }

                    List<Location> playerPath = playerLocation.getPath(playerEntity.getDestination(),
                            (l) -> !playerLocations.contains(l));
                    Location playerPathStep = playerPath.size() == 1 ? playerEntity.getDestination() : playerPath.get(0);

                    MovePitchEntity move = new MovePitchEntity(playerPathStep, playerEntity);
                    if (move.canExecute(pitch)) {
                        move.execute(pitch);
                    } else {
                        System.out.println("Can't execute " + move);
                    }

                    if (playerEntity.getDestination().equals(playerLocation)) {
                        SetPlayerConfig clear = new SetPlayerConfig(playerEntity, null);
                        if (clear.canExecute(pitch)) {
                            clear.execute(pitch);
                        }
                    }
                } catch (Throwable th) {
                    th.printStackTrace(System.err);
                }
            }
        }
    }
}
