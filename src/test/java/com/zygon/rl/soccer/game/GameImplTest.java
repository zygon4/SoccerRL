package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.PlayerGameStatus;
import com.zygon.rl.soccer.game.Game.TileItem;
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
        Game game = new GameImpl();
        game.start();

        Map<Location, Set<Game.TileItem>> initialPitchUpdates = game.getPitchUpdates();

        Map<PlayerGameStatus, Location> players = game.getPlayers();
        Map.Entry<PlayerGameStatus, Location> playerStatus = players.entrySet().stream()
                .findFirst()
                .orElseThrow();

        Player player = playerStatus.getKey().getPlayer();
        Location loc = playerStatus.getValue();

        Assert.assertTrue("Test bug. rerun.", game.getPlayer(new Location(0, 0)) == null);

        if (loc.getX() == 0 && loc.getY() == 0) {
            Assert.fail("test bug. rerun for this scenario.");
        }
        game.apply(PlayerAction.move(player, new Location(0, 0)));

        PlayerGameStatus movedPlayer = game.getPlayer(new Location(0, 0));

        Assert.assertTrue(movedPlayer.getPlayer().toString().equals(player.toString()));

//        Map<Location, Set<Game.TileItem>> pitchUpdates = game.getPitchUpdates();
//        Assert.assertEquals("Expected a pitch update", 1, pitchUpdates.size());
    }

    @Test
    public void testPitchUpates() {

        Game game = new GameImpl();
        game.start();

        Map<Location, Set<Game.TileItem>> initialPitchUpdates = game.getPitchUpdates();

        Map<PlayerGameStatus, Location> players = game.getPlayers();
        Map.Entry<PlayerGameStatus, Location> playerStatus = players.entrySet().stream()
                .findFirst()
                .orElseThrow();

        Player player = playerStatus.getKey().getPlayer();
        Location loc = playerStatus.getValue();

        Assert.assertTrue("Test bug. rerun.", game.getPlayer(new Location(0, 0)) == null);

        if (loc.getX() == 0 && loc.getY() == 0) {
            Assert.fail("test bug. rerun for this scenario.");
        }
        game.apply(PlayerAction.move(player, new Location(0, 0)));

        PlayerGameStatus movedPlayer = game.getPlayer(new Location(0, 0));

        Assert.assertTrue(movedPlayer.getPlayer() != null);

        Map<Location, Set<TileItem>> pitchUpdates = game.getPitchUpdates();
        // expect blank (from) and player w/ or w/o ball (to)
        Assert.assertEquals("Expected a pitch update", 2, pitchUpdates.size());
    }
}
