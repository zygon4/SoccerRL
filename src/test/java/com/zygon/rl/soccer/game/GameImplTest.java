package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.core.SoccerTile;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author zygon
 */
public class GameImplTest {

    @Test
    public void testMove() {
        Game game = new GameImpl(new GameConfiguration());
        game.start();

        Map<Location, Set<SoccerTile>> initialPitchUpdates = game.getPitchUpdates();

        Map<PlayerEntity, Location> players = game.getPlayers();
        Map.Entry<PlayerEntity, Location> playerStatus = players.entrySet().stream()
                .findFirst()
                .orElseThrow();

        Player player = playerStatus.getKey().getPlayer();
        Location loc = playerStatus.getValue();

        Assert.assertTrue("Test bug. rerun.", game.getPlayer(Location.create(0, 0)) == null);

        if (loc.getX() == 0 && loc.getY() == 0) {
            Assert.fail("test bug. rerun for this scenario.");
        }
        game.apply(PlayerAction.move(player, Location.create(0, 0)));

        PlayerEntity movedPlayer = game.getPlayer(Location.create(0, 0));

        Assert.assertTrue(movedPlayer.getPlayer().toString().equals(player.toString()));

//        Map<Location, Set<Game.TileItem>> pitchUpdates = game.getPitchUpdates();
//        Assert.assertEquals("Expected a pitch update", 1, pitchUpdates.size());
    }

    @Test
    public void testPitchUpates() {

        Game game = new GameImpl(new GameConfiguration());
        game.start();

        Map<Location, Set<SoccerTile>> initialPitchUpdates = game.getPitchUpdates();

        Map<PlayerEntity, Location> players = game.getPlayers();
        Map.Entry<PlayerEntity, Location> playerStatus = players.entrySet().stream()
                .findFirst()
                .orElseThrow();

        Player player = playerStatus.getKey().getPlayer();
        Location loc = playerStatus.getValue();

        Assert.assertTrue("Test bug. rerun.", game.getPlayer(Location.create(0, 0)) == null);

        if (loc.getX() == 0 && loc.getY() == 0) {
            Assert.fail("test bug. rerun for this scenario.");
        }
        game.apply(PlayerAction.move(player, Location.create(0, 0)));

        PlayerEntity movedPlayer = game.getPlayer(Location.create(0, 0));

        Assert.assertTrue(movedPlayer.getPlayer() != null);

        Map<Location, Set<SoccerTile>> pitchUpdates = game.getPitchUpdates();
        // expect blank (from) and player w/ or w/o ball (to)
        Assert.assertEquals("Expected a pitch update", 2, pitchUpdates.size());
    }
}
