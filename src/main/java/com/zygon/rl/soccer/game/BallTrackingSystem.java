package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.pitch.ApplyBallPressure;
import com.zygon.rl.soccer.core.pitch.MovePitchEntity;
import com.zygon.rl.soccer.core.pitch.Pitch;
import com.zygon.rl.soccer.core.pitch.PitchBall;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;

/**
 * Moves players towards their destinations.
 *
 */
public class BallTrackingSystem extends GameSystem {

    public BallTrackingSystem(GameConfiguration config) {
        super(config);
    }

    // See AISystem.java
    @Override
    public void accept(Game game, Pitch pitch) {

        PitchBall ball = pitch.getBall();
        if (ball.getForce() > 0) {
            Location dest = pitch.getBallLocation().getAngleNeighbor(ball.getAngle());

            PlayerEntity blockingPlayer = pitch.getPlayer(dest);
            if (blockingPlayer != null) {
                // TODO: intercepting player, friend or foe - TODO: due to ordering, may be *ourself*
            }

            MovePitchEntity moveBall = new MovePitchEntity(dest, ball);
            if (moveBall.canExecute(pitch)) {
                moveBall.execute(pitch);

                int residualForce = ball.getForce() - 1;
                ApplyBallPressure decelerate = new ApplyBallPressure(ball, residualForce, ball.getAngle());
                if (decelerate.canExecute(pitch)) {
                    decelerate.execute(pitch);
                }
            }

            // This check may be wrong if there's a player in the way.. ball should move freely
            // "power up" is based on speed and a general game mechanic, not specific to ball flight - "speed" and "energy" should be added to the PitchEntity
            //powerup
            //while (ballHasEnergy)  {
            //}
            //  coolDown
            //}
        }
    }
}
