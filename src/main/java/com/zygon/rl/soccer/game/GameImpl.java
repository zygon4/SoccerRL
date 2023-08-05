package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.LocationItems;
import com.zygon.rl.soccer.core.Pitch;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.PlayerGameStatus;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.strategy.Formations;
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
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class GameImpl implements Game {

    private final Random rand = new Random();
    private State state = State.PRE;
    private Pitch pitch = null;
    private final Map<Location, Set<TileItem>> updates = new HashMap<>();

    @Override
    public State getState() {
        return state;
    }

    @Override
    public void start() {
        checkState(State.PRE);

        pitch = new Pitch(
                createTeam("USA", Color.WHITE),
                createTeam("JAPAN", Color.RED),
                Formations._4_4_1_1);

        state = State.STARTED;

        // set goal location tiles, these never change.
        for (Location loc : pitch.getGoalLocations(pitch.getHomeTeam())) {
            updates.put(loc, Set.of(TileItem.GOAL));
        }
        for (Location loc : pitch.getGoalLocations(pitch.getAwayTeam())) {
            updates.put(loc, Set.of(TileItem.GOAL));
        }
    }

    @Override
    public void apply(UIAction action) {
        checkState(State.STARTED);

        switch (action.getAction()) {
            case HIGHLIGHT_PLAYER:
                // First remove all highlighting
                for (var entry : updates.entrySet()) {
                    Set<TileItem> items = entry.getValue();
                    if (items != null) {
                        Set<TileItem> removed = new LinkedHashSet<>(items);
                        removed.remove(TileItem.PLAYER_HIGHLIGHT);
                        updates.put(entry.getKey(), removed);
                    }
                }
                pitch.getLocation(action.getPlayer()).getRadius(1)
                        .forEach(l -> updates.put(l, Set.of(TileItem.PLAYER_HIGHLIGHT)));
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
                Pitch.PlayResult move = pitch.move(action.getPlayer(), action.getLocation());
                break;
            case TRACK:
                Pitch.PlayResult track = pitch.track(action.getPlayer(), action.getLocation());
                System.out.println(track);

                // TODO: which one??
                // pitch.track(player, location);
                break;
            case PASS:
                // TODO: handle result

                Pitch.PlayResult pass = null;

                Player teammate = action.getTeammate();
                if (teammate != null) {
                    pass = pitch.pass(teammate);
                } else {
                    pass = pitch.pass(action.getLocation());
                }

                if (pass.isGoal()) {
                    System.out.println("GOAAAALLLL!!!");
                } else {
                    System.out.println(pass);
                }
                break;
            case SHOOT:
                Pitch.PlayResult shoot = pitch.pass(action.getLocation());
                if (shoot.isGoal()) {
                    System.out.println("GOAAAALLLL!!!");
                } else {
                    System.out.println(shoot);
                }
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
                if (pitch.hasBall(player)) {
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
        return pitch.getBallLocation();
    }

    @Override
    public Map<Location, Set<TileItem>> getPitchUpdates() {
        checkState(State.STARTED);

        Map<Location, LocationItems> updatedItemsByLocation = pitch.getItemsByLocation();

        updatedItemsByLocation.forEach((newLoc, newLocItems) -> {
            Set<TileItem> oldItems = updates.get(newLoc);

            boolean hasUpdate = false;
            Set<TileItem> tileItems = new HashSet<>();

            if (oldItems == null) {
                hasUpdate = true;

                if (newLocItems.hasBall()) {
                    tileItems.add(TileItem.BALL);
                }

                if (newLocItems.getPlayer().isPresent()) {
                    tileItems.add(TileItem.PLAYER);
                }
            } else {
                if (newLocItems.hasBall() && !oldItems.contains(TileItem.BALL)) {
                    hasUpdate = true;
                    tileItems.add(TileItem.BALL);
                }

                if (!newLocItems.hasBall() && oldItems.contains(TileItem.BALL)) {
                    hasUpdate = true;
                    // TODO: remove ball from update location
                }

                if (newLocItems.getPlayer().isPresent() && !oldItems.contains(TileItem.PLAYER)) {
                    hasUpdate = true;
                    tileItems.add(TileItem.PLAYER);
                }

                if (newLocItems.getPlayer().isEmpty() && oldItems.contains(TileItem.PLAYER)) {
                    hasUpdate = true;
                }
            }

            // an empty update means those items are now gone
            if (hasUpdate) {
                updates.put(newLoc, tileItems);
            } else {
                if (convert(newLocItems).equals(oldItems)) {
                    updates.remove(newLoc);
                }
            }
        });

        return updates;
    }

    @Override
    public PlayerGameStatus getPlayer(Location location) {
        checkState(State.STARTED);

        return getPlayers().entrySet().stream()
                .filter(entry -> entry.getValue().equals(location))
                .map(Map.Entry::getKey)
                .findAny().orElse(null);
    }

    @Override
    public Map<PlayerGameStatus, Location> getPlayers() {
        checkState(State.STARTED);

        // this doesn't exactly feel efficient!
        return pitch.getItemsByLocation().entrySet().stream()
                .filter(entry -> entry.getValue().getPlayerGameStatus().isPresent())
                .map(entry -> Map.entry(entry.getValue().getPlayerGameStatus().get(), entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    @Override
    public boolean isGoal(Location location) {
        checkState(State.STARTED);

        // This is ALL goal locations!
        Set<Location> goalLocations = new HashSet<>();
        goalLocations.addAll(pitch.getGoalLocations(pitch.getHomeTeam()));
        goalLocations.addAll(pitch.getGoalLocations(pitch.getAwayTeam()));
        return goalLocations.contains(location);
    }

    @Override
    public void play() {
        checkState(State.STARTED);

        // TODO: Move the ball, update stats, etc
        pitch.getObjectiveActions().forEach(asyncAction -> {
            apply((PlayerAction) asyncAction);
        });

        // For both teams?
//        runPassDrillStep(pitch.getHomeTeam(), rand);
        runPassDrillStep(pitch.getAwayTeam(), rand);

//        if (game.teamHasPossession().getName().equals(playerTeam)) {
//            GameActions availableGameActions = game.getAvailable(game.teamHasPossession());
//
//            Action action = getAction(availableGameActions);
//
//            String argument = null;
//            if (action.hasArgument()) {
//                argument = getInput(action.getArgumentPrompt(), (String i) -> {
//                    String error = action.getArgumentError(i);
//                    if (error != null) {
//                        return error;
//                    }
//
//                    return null;
//                }, (String i) -> i);
//            }
//
//            game.apply(action, argument);
//        } else {
//            // AI turn
//            runPassDrillStep(game, rand);
//        }
    }

    private void checkState(State required) {
        if (state != required) {
            throw new IllegalStateException("Invalid state to start " + state);
        }
    }

    private static Set<TileItem> convert(LocationItems items) {
        Set<TileItem> updates = new HashSet<>();

        if (items.hasBall()) {
            updates.add(TileItem.BALL);
        }
        if (items.getPlayer().isPresent()) {
            updates.add(TileItem.PLAYER);
        }
        return updates;
    }

    private static Team createTeam(String name, Color color) {
        Random rand = new Random();

        Team team = new Team(name, color);

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
    private void runPassDrillStep(Team team, Random rand) {

        List<Player> players = new ArrayList<>(team.getPlayers());
        Collections.shuffle(players, rand);

        Player player = players.get(0);

//        for (Player p : players) {
//            if (player == null) {
//                Location playerLoc = pitch.getLocation(p);
//                LocationItems items = pitch.getItemsByLocation().get(playerLoc);
//                if (items.getPlayerGameStatus().get().getDestination() == null) {
//                    player = p;
//                    break;
//                }
//            }
//        }
//
//        if (player == null) {
//            // Everyone is busy..
//            return;
//        }
        Map<PlayerAction.Action, PlayerAction.Argument> actions = getAvailablePlayerActions(Set.of(player));
        PlayerAction.Action actionKey = actions.keySet().iterator().next();
        PlayerAction.Argument actionArg = actions.get(actionKey);

        Map<Integer, PlayerAction> rankedActions = new TreeMap<>();

        for (var action : actions.entrySet()) {
            switch (action.getKey()) {
                case TRACK -> {
                    if (!pitch.hasBall(player)) {
                        rankedActions.put(1, PlayerAction.track(player, pitch.getBallLocation()));
                    } else {
                        rankedActions.put(2, PlayerAction.track(player, pitch.getGoalLocations(pitch.getOpponent(team)).get(0)));
                    }
                }
                case PASS -> // This is bad..
                    rankedActions.put(3, PlayerAction.pass(player, pitch.getRandomLocation()));
                case SHOOT -> {
                    if (pitch.getGoalLocations(pitch.getOpponent(team)).get(0).getDistance(pitch.getLocation(player)) <= 10) {
                        List<Location> goalLocations = new ArrayList<>(pitch.getGoalLocations(pitch.getOpponent(team)));
                        Collections.shuffle(goalLocations, rand);
                        rankedActions.put(1, PlayerAction.shoot(player, goalLocations.get(0)));
                    } else {
                        rankedActions.put(2, PlayerAction.track(player, pitch.getGoalLocations(pitch.getOpponent(team)).get(0)));
                    }
                }
            }
        }

//        switch (actionKey) {
//            case TRACK:
//
//                pitch.playerAction = PlayerAction.track(player, pitch.getBallLocation());
//                break;
//            case PASS:
//                switch (actionArg) {
//                    case LOCATION:
//                        rankedActions = PlayerAction.pass(player, pitch.getRandomLocation());
//                        break;
//                    case PLAYER:
//                        // TODO: teammate
//                        rankedActions = PlayerAction.pass(player, player);
//                        break;
//                }
//                break;
//            case SHOOT:
//                List<Location> goalLocations = new ArrayList<>(pitch.getGoalLocations(pitch.getOpponent(team)));
//                Collections.shuffle(goalLocations, rand);
//                rankedActions = PlayerAction.shoot(player, goalLocations.get(0));
//                break;
//            default:
//        }
        Integer key = rankedActions.keySet().iterator().next();
        PlayerAction playerAction = rankedActions.get(key);
        apply(playerAction);

//        GameActions availableGameActions = getAvailable(pitch.teamHasPossession());
//
//        System.out.println("Available actions:");
//        System.out.println(availableGameActions);
//
//        PlayerAction playerAction = null;
//
//        // 15% chance of shooting, otherwise pass
//        if (rand.nextDouble() < .85) {
//            List<PlayerAction> randomPlayerAction = availableGameActions.getPlayerActions().stream()
//                    .filter(pa -> pa.getAction() != PlayerAction.Action.SHOOT && pa.getAction() != PlayerAction.Action.TRACK)
//                    .collect(Collectors.toList());
//            Collections.shuffle(randomPlayerAction);
//
//            playerAction = randomPlayerAction.get(0);
//        } else {
//            // hardcoded location to shoot at
//            Location goalLocation = pitch.getGoalLocations(pitch.defendingTeam()).get(3);
//            playerAction = PlayerAction.shoot(pitch.playerHasPossession(), goalLocation);
//        }
//
//        // apply to game
//        pitch.apply(playerAction, null);
    }
}
