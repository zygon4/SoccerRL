package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Ball;
import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.core.pitch.AddPitchEntity;
import com.zygon.rl.soccer.core.pitch.MovePitchEntity;
import com.zygon.rl.soccer.core.pitch.Pitch;
import com.zygon.rl.soccer.core.pitch.PitchBall;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.game.strategy.FormationHelper;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keeps score.
 *
 */
public class ScoreTrackingSystem extends GameSystem {

    private final Team homeTeam;
    private final Team awayTeam;
    private final Map<Team, Score> scoreByTeam;
    private final Supplier<Team> getScoringTeamForBallLocation;

    public ScoreTrackingSystem(GameConfiguration config, Team homeTeam,
            Team awayTeam, Supplier<Team> getScoringTeam) {
        super(config);

        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.scoreByTeam = Map.of(this.homeTeam, new Score(this.homeTeam),
                this.awayTeam, new Score(this.awayTeam));
        this.getScoringTeamForBallLocation = getScoringTeam;
    }

    // See AISystem.java
    @Override
    public void accept(Game game, Pitch pitch) {

        Team scoringTeam = getScoringTeamForBallLocation.get();

        if (scoringTeam != null) {
            Score score = scoreByTeam.get(scoringTeam);
            score.addGoal();

            fillPitch(pitch, this.homeTeam, this.awayTeam);
        }
    }

    static void fillPitch(Pitch pitch, Team home, Team away) {

        resetPitch(pitch, home, false);
        resetPitch(pitch, away, true);

        int ballStartX = Pitch.PITCH_FIELD_WIDTH_OFFSET + Pitch.PITCH_WIDTH / 2;
        int ballStartY = Pitch.PITCH_FIELD_HEIGHT_OFFSET + Pitch.PITCH_HEIGHT / 2;
        Location loc = Location.create(ballStartX, ballStartY);
        PitchBall ball = new PitchBall(new Ball(0, 0, 3.0));

        if (pitch.getBallLocation() == null) {
            Pitch.PitchAction addBall = new AddPitchEntity(loc, ball);
            if (addBall.canExecute(pitch)) {
                addBall.execute(pitch);
            }
        } else {
            Pitch.PitchAction moveBall = new MovePitchEntity(loc, ball);
            if (moveBall.canExecute(pitch)) {
                moveBall.execute(pitch);
            }
        }
    }

    Map<Team, Score> getScoreByTeam() {
        return scoreByTeam;
    }

    // from height of 0
    static Set<Location> resetPitch(Pitch pitch, Team team, boolean reversed) {

        Iterator<Player> players = team.getPlayers().iterator();

        FormationHelper helper = new FormationHelper(team.getFormation(), Pitch.PITCH_HEIGHT, Pitch.PITCH_WIDTH);
        Set<Location> zoneLocations = helper.getPlayerPitchLocations(Pitch.PITCH_HEIGHT / 2);

        for (Location loc : zoneLocations) {

            Location trueLocation = Location.create(
                    loc.getX() + Pitch.PITCH_FIELD_WIDTH_OFFSET,
                    loc.getY() + Pitch.PITCH_FIELD_HEIGHT_OFFSET);

            if (reversed) {
                int reverse = Pitch.PITCH_FIELD_HEIGHT_OFFSET + Pitch.PITCH_HEIGHT - loc.getY() - 1;
                trueLocation = trueLocation.setY(reverse);
            }

            Player player = null;
            try {
                player = players.next();
            } catch (Throwable th) {
                th.printStackTrace();
            }

            PlayerEntity p = new PlayerEntity(player);
            if (pitch.getLocation(p) == null) {
                Pitch.PitchAction action = new AddPitchEntity(trueLocation, p);
                if (action.canExecute(pitch)) {
                    action.execute(pitch);
                }
            } else {
                Pitch.PitchAction move = new MovePitchEntity(trueLocation, p);
                if (move.canExecute(pitch)) {
                    move.execute(pitch);
                }
            }
        }

        return zoneLocations;
    }
}
