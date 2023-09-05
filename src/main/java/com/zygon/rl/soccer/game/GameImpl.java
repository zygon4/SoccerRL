package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Formation;
import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.SoccerTile;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.core.pitch.Layout;
import com.zygon.rl.soccer.core.pitch.MovePitchEntity;
import com.zygon.rl.soccer.core.pitch.Pitch;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.core.pitch.SetPlayerConfig;
import com.zygon.rl.soccer.game.strategy.Formations;
import com.zygon.rl.soccer.ui.UIAction;
import com.zygon.rl.soccer.utils.Utils;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 */
public class GameImpl implements Game {

    private final Team homeTeam = createTeam("USA", Color.WHITE, Formations._4_2_3_1);
    private final Team awayTeam = createTeam("JAPAN", Color.RED, Formations._4_4_1_1);
    private final Map<Team, List<Location>> orderedGoalLocationsByTeam = new HashMap<>(2);
    private final ConcurrentHashMap<Location, Set<SoccerTile>> updates = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Location, Location> uiEffectslocations = new ConcurrentHashMap<>();

    private final Pitch pitch = new Pitch();
    private final Layout layout;
    private final ScoreTrackingSystem scoreTrackingSystem;
    private final List<GameSystem> systems;
    private State state = State.PRE;

    public GameImpl(GameConfiguration config) {

        this.layout = Layout.constructPitch(
                Pitch.FIELD_WIDTH, Pitch.FIELD_HEIGHT,
                Pitch.PITCH_WIDTH, Pitch.PITCH_HEIGHT,
                Pitch.PITCH_FIELD_WIDTH_OFFSET, Pitch.PITCH_FIELD_HEIGHT_OFFSET);

        orderedGoalLocationsByTeam.put(homeTeam, new ArrayList<>());
        orderedGoalLocationsByTeam.put(awayTeam, new ArrayList<>());

        orderedGoalLocationsByTeam.get(homeTeam).addAll(this.layout.getHalfAGoals());
        orderedGoalLocationsByTeam.get(awayTeam).addAll(this.layout.getHalfBGoals());

        ScoreTrackingSystem.fillPitch(pitch, homeTeam, awayTeam);

        this.scoreTrackingSystem = new ScoreTrackingSystem(config, homeTeam, awayTeam,
                getScoringTeam(pitch, homeTeam, awayTeam, orderedGoalLocationsByTeam));

        this.systems = List.of(new PlayerTrackingSystem(config), new BallTrackingSystem(config), scoreTrackingSystem);
    }

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void start() {
        checkState(State.PRE);

        for (int y = 0; y < Pitch.FIELD_HEIGHT; y++) {
            for (int x = 0; x < Pitch.FIELD_WIDTH; x++) {
                Location newLoc = Location.create(x, y);
                SoccerTile defaultTile = this.layout.getTile(newLoc);
                Set<SoccerTile> tiles = updates.computeIfAbsent(newLoc, l -> new LinkedHashSet<>());
                tiles.add(defaultTile);
            }
        }
        state = State.STARTED;
    }

    @Override
    public void apply(UIAction action) {
        checkState(State.STARTED);

        switch (action.getAction()) {
            case HIGHLIGHT_PLAYER:
                pitch.getPlayerLocations().forEach(l -> {
                    PlayerEntity player = pitch.getPlayer(l);
                    if (player.isHighlighted()) {
                        apply(UIAction.unHighlightPlayer(player.getPlayer()));
                    }
                });

                // This get-location-get-player is odd
                Location highLocation = pitch.getLocation(new PlayerEntity(action.getPlayer()));
                PlayerEntity highPlayer = pitch.getPlayer(highLocation);
                if (!highPlayer.isHighlighted()) {
                    SetPlayerConfig playerConfig = new SetPlayerConfig(highPlayer, true);
                    playerConfig.execute(pitch);
                }

                break;
            case HIGHLIGHT_PATH:
                // TODO: set path tiles in updates and update the removal
                // TODO: more support and options

//                action.getSource().getPath(action.getTarget())
//                        .forEach(l -> updates.put(l, Set.of(TileItem.PLAYER_TRACK)));
                break;
            case UNHIGHLIGHT_PLAYER:
                // This get-location-get-player is odd
                Location unHighLocation = pitch.getLocation(new PlayerEntity(action.getPlayer()));
                PlayerEntity unHighPlayer = pitch.getPlayer(unHighLocation);
                if (unHighPlayer.isHighlighted()) {
                    SetPlayerConfig playerConfig = new SetPlayerConfig(unHighPlayer, false);
                    playerConfig.execute(pitch);
                }
                break;
        }
    }

    @Override
    public void apply(PlayerAction action) {
        checkState(State.STARTED);

        switch (action.getAction()) {
            case MOVE:
                MovePitchEntity move = new MovePitchEntity(action.getLocation(), new PlayerEntity(action.getPlayer()));
                if (move.canExecute(pitch)) {
                    move.execute(pitch);
                }
                break;
            case TRACK:
                // This is a little strange, too much indirection due to outer wrapping "PlayerEntity"
                Location playerLoc = pitch.getLocation(new PlayerEntity(action.getPlayer()));
                PlayerEntity player = pitch.getPlayer(playerLoc);
                if (!player.hasDestination() || !player.getDestination().equals(action.getLocation())) {
                    SetPlayerConfig track = new SetPlayerConfig(player, action.getLocation());
                    if (track.canExecute(pitch)) {
                        track.execute(pitch);
                    }
                }
                break;
            case PASS:
                // TODO:
                // TODO: handle result

//                Pitch.PlayResult pass = null;
//
//                Player teammate = action.getTeammate();
//                if (teammate != null) {
//                    pass = pitch.pass(teammate);
//                } else {
//                    pass = pitch.pass(action.getLocation());
//                }
//
//                if (pass.isGoal()) {
//                    System.out.println("GOAAAALLLL!!!");
//                } else {
//                    System.out.println(pass);
//                }
                break;
            case SHOOT:
                // TODO:
//                Pitch.PlayResult shoot = pitch.pass(action.getLocation());
//                if (shoot.isGoal()) {
//                    System.out.println("GOAAAALLLL!!!");
//                } else {
//                    System.out.println(shoot);
//                }
                break;
            default:
                throw new UnsupportedOperationException(action.getAction().name());
        }
    }

    @Override
    public Map<PlayerAction.Action, PlayerAction.Argument> getAvailablePlayerActions(
            Collection<Player> players) {
        checkState(State.STARTED);

        Map<PlayerAction.Action, PlayerAction.Argument> actions = new LinkedHashMap<>();

        if (!players.isEmpty()) {
            // If a group, they all goto the space area
            actions.put(PlayerAction.Action.TRACK, PlayerAction.Argument.LOCATION);
            // A single player will tail someone, a group will box that person
            actions.put(PlayerAction.Action.TAIL, PlayerAction.Argument.PLAYER);

            if (players.size() == 1) {
                Player player = players.iterator().next();

                // single player actions
                if (pitch.hasBall(new PlayerEntity(player))) {
                    actions.put(PlayerAction.Action.PASS, PlayerAction.Argument.LOCATION);
                    actions.put(PlayerAction.Action.SHOOT, PlayerAction.Argument.LOCATION);
                }
            } else {
                // group actions
                actions.put(PlayerAction.Action.MOVE_UNISON, PlayerAction.Argument.LOCATION);
            }
        }

        return actions;
    }

    @Override
    public Location getBall() {
        checkState(State.STARTED);

        return pitch.getBallLocation();
    }

    @Override
    public Map<Location, Set<SoccerTile>> getPitchUpdates() {
        checkState(State.STARTED);

        Location ballLocation = pitch.getBallLocation();

        Set<Location> locationChanges = new LinkedHashSet<>();
        locationChanges.addAll(pitch.getLocationChanges(true));
        locationChanges.addAll(uiEffectslocations.keySet());
        uiEffectslocations.clear();

        try {
            for (Location locChange : locationChanges) {
                PlayerEntity player = pitch.getPlayer(locChange);
                boolean hasBall = locChange.equals(ballLocation);
                Set<SoccerTile> tileItems = updates.computeIfAbsent(locChange,
                        l -> new LinkedHashSet<>());
                tileItems.addAll(convert(player != null, hasBall));

                if (player != null) {
                    if (player.isHighlighted()) {
                        uiEffectslocations.put(locChange, locChange);
                        pitch.getNeighborLocations(locChange)
                                .forEach(loc -> {
                                    Set<SoccerTile> tiles = updates.computeIfAbsent(loc, l -> new LinkedHashSet<>());
                                    tiles.add(SoccerTile.PLAYER_HIGHLIGHT);
                                    // Note the places where effects happen to clear them for the next pass
                                    uiEffectslocations.put(loc, loc);
                                });
                    }
                }
                if (tileItems.isEmpty()) {
                    tileItems.add(layout.getTile(locChange));
                }
            }
        } catch (Throwable th) {
            th.printStackTrace();
        }

        Map<Location, Set<SoccerTile>> ups = new HashMap<>(updates);
        updates.clear();
        return ups;
    }

    @Override
    public PlayerEntity getPlayer(Location location) {
        checkState(State.STARTED);

        return getPlayers().entrySet().stream()
                .filter(entry -> entry.getValue().equals(location))
                .map(Map.Entry::getKey)
                .findAny().orElse(null);
    }

    @Override
    public Map<PlayerEntity, Location> getPlayers() {
        checkState(State.STARTED);

        return pitch.getPlayerLocations().stream()
                .collect(Collectors.toMap(pitch::getPlayer, l -> l));
    }

    @Override
    public Map<Team, Score> getScores() {
        return Collections.unmodifiableMap(scoreTrackingSystem.getScoreByTeam());
    }

    @Override
    public boolean isGoal(Location location) {
        checkState(State.STARTED);

        // This is ALL goal locations!
        Set<Location> goalLocations = new HashSet<>();
        goalLocations.addAll(orderedGoalLocationsByTeam.get(homeTeam));
        goalLocations.addAll(orderedGoalLocationsByTeam.get(awayTeam));
        return goalLocations.contains(location);
    }

    @Override
    public void play() {
        checkState(State.STARTED);

        for (GameSystem system : systems) {
            system.accept(this, pitch);
        }
    }

    private void checkState(State required) {
        if (state != required) {
            throw new IllegalStateException("Invalid state to start " + state);
        }
    }

    private static Set<SoccerTile> convert(boolean hasPlayer, boolean hashBall) {
        Set<SoccerTile> updates = new LinkedHashSet<>();
        if (hasPlayer) {
            updates.add(SoccerTile.PLAYER);
        }
        if (hashBall) {
            updates.add(SoccerTile.BALL);
        }
        return updates;
    }

    private static Team createTeam(String name, Color color, Formation formation) {
        Random rand = new Random();

        Team team = new Team(name, color, formation);

        // TODO: add keeper
        for (int i = 0; i < 10; i++) {

            int playerNumber = -1;
            do {
                playerNumber = rand.nextInt(10);
                playerNumber = ((i == 0 ? 1 : i) * 10) + playerNumber;
            } while (team.hasPlayer(playerNumber));

            Player player = new Player(
                    playerNumber,
                    team,
                    getStat(rand),
                    getStat(rand),
                    getStat(rand),
                    getStat(rand));
            team.add(player);
        }

        return team;
    }

    // kinda goofy..
    private static Supplier<Team> getScoringTeam(Pitch pitch, Team homeTeam,
            Team awayTeam, Map<Team, List<Location>> orderedGoalLocationsByTeam) {

        Map<Team, Team> opponent = Map.of(homeTeam, awayTeam, awayTeam, homeTeam);
        Map<Location, Team> teamsGoal = new HashMap<>();
        orderedGoalLocationsByTeam.get(homeTeam).forEach(homeTeamGoals -> {
            teamsGoal.put(homeTeamGoals, homeTeam);
        });
        orderedGoalLocationsByTeam.get(awayTeam).forEach(awayTeamGoals -> {
            teamsGoal.put(awayTeamGoals, awayTeam);
        });

        return () -> {
            Location ballLoc = pitch.getBallLocation();

            Team teamGoal = teamsGoal.get(ballLoc);
            if (teamGoal != null) {
                return opponent.get(teamGoal);
            }

            return null;
        };
    }

    // This is pretty trash
    private static double getStat(Random rand) {

        double gaussVal = rand.nextGaussian();
        double posGauss = Math.sqrt(gaussVal * gaussVal);
        double scaledGauss = posGauss / 1.0;

        double val = Math.max(scaledGauss, .50);
        val = Math.min(val, 1.0);

        return Utils.round(val);
    }
}
