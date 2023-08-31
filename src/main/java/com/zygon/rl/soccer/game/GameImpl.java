package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.SoccerTile;
import com.zygon.rl.soccer.core.Formation;
import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.Team;
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

    private static final int GOAL_WIDTH = 5 * Pitch.PITCH_SCALE;

    private final Team homeTeam = createTeam("USA", Color.WHITE, Formations._4_2_3_1);
    private final Team awayTeam = createTeam("JAPAN", Color.RED, Formations._4_4_1_1);
    private final Map<Team, List<Location>> orderedGoalLocationsByTeam = new HashMap<>(2);
    private final ConcurrentHashMap<Location, Set<SoccerTile>> updates = new ConcurrentHashMap<>();

    private final Pitch pitch = new Pitch();
    private final ScoreTrackingSystem scoreTrackingSystem;
    private final List<GameSystem> systems;
    private State state = State.PRE;

    public GameImpl(GameConfiguration config) {

        // The goal "hitbox" is right in front of the goals. This is beause there's
        // an issue with the path finding from positive to negative grid space and
        // this is just easier.
        int startingWidth = (Pitch.WIDTH / 2) - (GOAL_WIDTH / 2);

        List<Location> homeTeamGoals = new ArrayList<>();
        for (int i = startingWidth; i < startingWidth + GOAL_WIDTH; i++) {
            Location l = Location.create(i, 0);
            homeTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(homeTeam, homeTeamGoals);

        List<Location> awayTeamGoals = new ArrayList<>();
        for (int i = startingWidth; i < startingWidth + GOAL_WIDTH; i++) {
            Location l = Location.create(i, Pitch.HEIGHT - 1);
            awayTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(awayTeam, awayTeamGoals);

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

        // set goal location tiles, these never change.
        for (Location loc : orderedGoalLocationsByTeam.get(homeTeam)) {
            updates.put(loc, Set.of(SoccerTile.GOAL));
        }
        for (Location loc : orderedGoalLocationsByTeam.get(awayTeam)) {
            updates.put(loc, Set.of(SoccerTile.GOAL));
        }

        state = State.STARTED;
    }

    @Override
    public void apply(UIAction action) {
        checkState(State.STARTED);

        switch (action.getAction()) {
            case HIGHLIGHT_PLAYER:
                // First remove all highlighting
                for (Location key : updates.keySet()) {
                    Set<SoccerTile> items = updates.get(key);
                    if (items != null) {

                        Set<SoccerTile> removed = new LinkedHashSet<>(items);
                        removed.remove(SoccerTile.PLAYER_HIGHLIGHT);
                        updates.merge(key, removed, (s1, s2) -> s1.size() < s2.size() ? s1 : s2);
                    }
                }

                pitch.getNeighborLocations(pitch.getLocation(new PlayerEntity(action.getPlayer())))
                        .forEach(l -> updates.put(l, Set.of(SoccerTile.PLAYER_HIGHLIGHT)));
                break;
            case HIGHLIGHT_PATH:
                // TODO: set path tiles in updates and update the removal
                // TODO: more support and options

//                action.getSource().getPath(action.getTarget())
//                        .forEach(l -> updates.put(l, Set.of(TileItem.PLAYER_TRACK)));
                break;
//            case UNHIGHLIGHT_PLAYER:
//                pitch.getLocation(action.getPlayer()).getRadius(1)
//                        .forEach(l -> {
//                            Set<TileItem> items = updates.get(l);
//                            if (items != null) {
//                                items.remove(TileItem.PLAYER_HIGHLIGHT);
//                            }
//                        });
//                break;
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
                SetPlayerConfig track = new SetPlayerConfig(new PlayerEntity(action.getPlayer()), action.getLocation());
                if (track.canExecute(pitch)) {
                    track.execute(pitch);
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

        // set goal location tiles, these never change.
        for (Location loc : orderedGoalLocationsByTeam.get(homeTeam)) {
            updates.put(loc, Set.of(SoccerTile.GOAL));
        }
        for (Location loc : orderedGoalLocationsByTeam.get(awayTeam)) {
            updates.put(loc, Set.of(SoccerTile.GOAL));
        }

        // TODO: Feels awful to do a full loop..
        Location ballLocation = pitch.getBallLocation();
        for (int y = 0; y < Pitch.HEIGHT; y++) {
            for (int x = 0; x < Pitch.WIDTH; x++) {
                Location newLoc = Location.create(x, y);

                PlayerEntity player = pitch.getPlayer(newLoc);
                boolean hasBall = newLoc.equals(ballLocation);

                Set<SoccerTile> oldItems = updates.get(newLoc);

                boolean hasUpdate = false;
                Set<SoccerTile> tileItems = new HashSet<>();

                if (oldItems == null) {
                    hasUpdate = true;

                    if (hasBall) {
                        tileItems.add(SoccerTile.BALL);
                    }

                    if (player != null) {
                        tileItems.add(SoccerTile.PLAYER);
                    }
                } else {
                    if (hasBall && !oldItems.contains(SoccerTile.BALL)) {
                        hasUpdate = true;
                        tileItems.add(SoccerTile.BALL);
                    }

                    if (!hasBall && oldItems.contains(SoccerTile.BALL)) {
                        hasUpdate = true;
                        // TODO: remove ball from update location
                    }

                    if (player != null && !oldItems.contains(SoccerTile.PLAYER)) {
                        hasUpdate = true;
                        tileItems.add(SoccerTile.PLAYER);
                    }

                    if (player == null && oldItems.contains(SoccerTile.PLAYER)) {
                        hasUpdate = true;
                    }
                }

                // an empty update means those items are now gone
                if (hasUpdate) {
                    updates.put(newLoc, tileItems);
                } else {
                    if (convert(true, hasBall).equals(oldItems)) {
                        updates.remove(newLoc);
                    }
                }
            }
        }

        return updates;
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

        //
        // TODO: Move the ball, update stats, etc
//        pitch.getObjectiveActions().forEach(asyncAction -> {
//            apply((PlayerAction) asyncAction);
//        });
        // For both teams for now, let them play. Human can change directions.
//        runPassDrillStep(homeTeam, rand);
//        runPassDrillStep(awayTeam, rand);
    }

    private void checkState(State required) {
        if (state != required) {
            throw new IllegalStateException("Invalid state to start " + state);
        }
    }

    private static Set<SoccerTile> convert(boolean hasPlayer, boolean hashBall) {
        Set<SoccerTile> updates = new HashSet<>();

        if (hashBall) {
            updates.add(SoccerTile.BALL);
        }
        if (hasPlayer) {
            updates.add(SoccerTile.PLAYER);
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

    // TODO: this has become the crude AI
    // TODO: convert to GameSystem
//    private void runPassDrillStep(Team team, Random rand) {
//
//        List<Player> players = new ArrayList<>(team.getPlayers());
//        Collections.shuffle(players, rand);
//
//        Player player = players.get(0);
//
//        Map<PlayerAction.Action, PlayerAction.Argument> actions = getAvailablePlayerActions(Set.of(player));
//        PlayerAction.Action actionKey = actions.keySet().iterator().next();
//        PlayerAction.Argument actionArg = actions.get(actionKey);
//
//        Map<Integer, PlayerAction> rankedActions = new TreeMap<>();
//
//        for (var action : actions.entrySet()) {
//            switch (action.getKey()) {
//                case TRACK -> {
//                    if (!NEWPitch.getLocation(new PlayerEntity(player)).equals(NEWPitch.getBallLocation())) {
//                        rankedActions.put(1, PlayerAction.track(player, pitch.getBallLocation()));
//                    } else {
//                        rankedActions.put(2, PlayerAction.track(player, pitch.getGoalLocations(pitch.getOpponent(team)).get(0)));
//                    }
//                }
//                case PASS -> // This is bad..
//                    rankedActions.put(3, PlayerAction.pass(player, pitch.getRandomLocation()));
//                case SHOOT -> {
//                    if (orderedGoalLocationsByTeam.get(pitch.getOpponent(team)).get(0).getDistance(pitch.getLocation(player)) <= 10) {
//                        List<Location> goalLocations = new ArrayList<>(pitch.getGoalLocations(pitch.getOpponent(team)));
//                        Collections.shuffle(goalLocations, rand);
//                        rankedActions.put(1, PlayerAction.shoot(player, goalLocations.get(0)));
//                    } else {
//                        rankedActions.put(2, PlayerAction.track(player, pitch.getGoalLocations(pitch.getOpponent(team)).get(0)));
//                    }
//                }
//            }
//        }
//
//        Integer key = rankedActions.keySet().iterator().next();
//        PlayerAction playerAction = rankedActions.get(key);
//        apply(playerAction);
//    }
}
