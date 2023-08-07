package com.zygon.rl.soccer.core;

import com.zygon.rl.soccer.core.Ball;
import com.zygon.rl.soccer.strategy.FormationHelper;
import com.zygon.rl.soccer.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class Pitch {

    public static final class PlayResult {

        private final String playNameDisplay;
        private final Player player;
        private final Location target;
        private final boolean goal;
        private final Player targetPlayer;
        private final Player defender;

        private PlayResult(String playNameDisplay, Player player,
                Location target, Player targetPlayer, Player defender,
                boolean goal) {

            this.playNameDisplay = Objects.requireNonNull(playNameDisplay);
            this.player = Objects.requireNonNull(player);
            this.target = Objects.requireNonNull(target);
            this.targetPlayer = targetPlayer;
            this.defender = defender;
            this.goal = goal;

            if (defender != null && goal) {
                throw new IllegalStateException();
            }
        }

        public static PlayResult move(Player player, Location target) {
            return new PlayResult("moves", player, target, null, null, false);
        }

        public static PlayResult pass(Player passer, Location target,
                Player targetPlayer) {
            return new PlayResult("passes", passer, target, targetPlayer, null, false);
        }

        public static PlayResult passDefended(Player passer, Location target,
                Player targetPlayer, Player defender) {
            return new PlayResult("passes", passer, target, targetPlayer, defender, false);
        }

        public static PlayResult moveDefended(Player player, Location target,
                Player targetPlayer, Player defender) {
            // TODO: better name
            return new PlayResult("blocked", player, target, targetPlayer, defender, false);
        }

        public static PlayResult goal(Player passer, Location target) {
            return new PlayResult("shoots", passer, target, null, null, true);
        }

        public static PlayResult track(Player player, Location target) {
            return new PlayResult("tracks", player, target, null, null, false);
        }

        public boolean isGoal() {
            return goal;
        }

        public Player getPlayer() {
            return player;
        }

        public Location getTarget() {
            return target;
        }

        public Optional<Player> getTargetPlayer() {
            return Optional.ofNullable(targetPlayer);
        }

        public Optional<Player> getDefendingPlayer() {
            return Optional.ofNullable(defender);
        }

        public String getDisplayString() {
            // TODO: pass metrics
            String distanceToTargetStr = "TODO";

            StringBuilder sb = new StringBuilder();

            sb.append("#").append(player.getNumber()).append(" of ").append(player.getTeam().getName());

            // passes to, shoot at
            if (getTargetPlayer().isPresent()) {
                // passes to
                Player tarPlayer = getTargetPlayer().get();
                sb.append(" ").append(playNameDisplay).append(" to #").append(tarPlayer.getNumber());
            } else {
                sb.append(" ").append(playNameDisplay).append(" to ").append(getTarget());
            }

            if (getDefendingPlayer().isPresent()) {
                Player defPlayer = getDefendingPlayer().get();
                sb.append(" intercepted by #")
                        .append(defPlayer.getNumber())
                        .append(" of ")
                        .append(defPlayer.getTeam().getName());
            }

            if (goal) {
                sb.append(" GOAL!");
            }

            return sb.toString();
        }

        @Override
        public String toString() {
            return getDisplayString();
        }
    }

    private static final int PITCH_SCALE = 2;
    private static final int HEIGHT = 30 * PITCH_SCALE; // not including the goal
    private static final int WIDTH = 20 * PITCH_SCALE;
    private static final int GOAL_WIDTH = 5 * PITCH_SCALE;

    // TBD: experimental
    public static enum Sidebar {
        PLAYER_INFO,
        TEAMS;
    }

    private final Location[][] pitch = new Location[WIDTH][HEIGHT];
    private final Map<Team, List<Location>> orderedGoalLocationsByTeam = new HashMap<>(2);
    private final Map<Location, LocationItems> itemsByLocation = new HashMap<>();
    private final Team homeTeam;
    private final Team awayTeam;
    private final Formation defaultFormation;
    private final List<String> gameLog = new ArrayList<>();
    private final Random rand = new Random();
    private Location ballLocation = null;
    private Sidebar sidebar = Sidebar.TEAMS;
    private Player sidebarPlayer = null; // optional

    public Pitch(Team home, Team away, Formation defaultFormation) {
        this.homeTeam = home;
        this.awayTeam = away;
        this.defaultFormation = defaultFormation;

        fillPitch(this.homeTeam, this.awayTeam, this.defaultFormation);
    }

    public Team getHomeTeam() {
        return homeTeam;
    }

    public Team getAwayTeam() {
        return awayTeam;
    }

    public List<String> getGameLog() {
        return Collections.unmodifiableList(gameLog);
    }

    // This won't really scale for "dynamic" movement by many players
    // We want each player on the pitch to have a destination and for them
    // to move their each turn.
    public Collection<Location> getLegalMoves(Player player) {
        Collection<Location> legalMoves = new ArrayList<>();

        for (Location location : getNeighborLocations(getLocation(player))) {
            LocationItems locationItems = getLocationItems(location);
            if (locationItems != null && (locationItems.getPlayer() == null || locationItems.getPlayer().isEmpty())) {
                legalMoves.add(location);
            }
        }

        return legalMoves;
    }

    /**
     * Returns the nearest neighbors. HOWEVER, this only returns the n/s/e/w, no
     * diagonals. TODO: add diagonals.
     *
     * @param location
     * @return
     */
    public Collection<Location> getNeighborLocations(Location location) {
        return location.getNeighbors(1);
    }

    public List<Location> getGoalLocations(Team team) {
        return Collections.unmodifiableList(orderedGoalLocationsByTeam.get(team));
    }

    public Team defendingTeam() {
        return getOpponent(teamHasPossession());
    }

    public Team teamHasPossession() {
        return playerHasPossession().getTeam();
    }

    public Player playerHasPossession() {
        return getLocationItems(ballLocation).getPlayer().get();
    }

    public Location getBallLocation() {
        return ballLocation;
    }

    public Location getLocation(Player player) {

        // double for loop :(
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                LocationItems locationItems = getLocationItems(pitch[x][y]);

                if (locationItems != null && locationItems.getPlayer().isPresent()) {
                    if (locationItems.getPlayer().get().equals(player)) {
                        return pitch[x][y];
                    }
                }
            }
        }

        return null;
    }

    public Map<Location, LocationItems> getItemsByLocation() {
        return Collections.unmodifiableMap(itemsByLocation);
    }

    public Team getOpponent(Team team) {
        return team.equals(homeTeam) ? awayTeam : homeTeam;
    }

    public Location getRandomLocation() {
        int height = rand.nextInt(HEIGHT);
        int width = rand.nextInt(WIDTH);
        return pitch[width][height];
    }

    public boolean hasBall(Player player) {
        Location playerLocation = getLocation(player);
        return playerLocation.equals(ballLocation);
    }

    // Move is similar to a short pass. neighboring defenders can still intercept.
    // TODO: add defense
    public PlayResult move(Player player, Location location) {
        // Start with just a move, no defense.

        if (location.getX() + 1 > WIDTH || location.getX() < 0 || location.getY() + 1 > HEIGHT || location.getY() < 0) {
            throw new IllegalArgumentException("Cannot move to " + location);
        }

        Location playerLocation = getLocation(player);

        boolean playerHasBall = hasBall(player);

        Optional<PlayerGameStatus> playerGameStatus = getLocationItems(playerLocation).getPlayerGameStatus();
        Optional<PlayerGameStatus> locationGameStatus = getLocationItems(location).getPlayerGameStatus();

        // TODO: can move?
        if (locationGameStatus.isPresent()) {
            return PlayResult.moveDefended(player, location,
                    locationGameStatus.get().getPlayer(),
                    locationGameStatus.get().getPlayer());
        }

        getLocationItems(playerLocation).setPlayerGameStatus(null);
        getLocationItems(location).setPlayerGameStatus(playerGameStatus.get());

        if (playerGameStatus.get().getDestination() != null
                && playerGameStatus.get().getDestination().equals(location)) {
            getLocationItems(location).setPlayerGameStatus(playerGameStatus.get().setDestination(null));
        }

        if (playerHasBall) {
            Ball ball = getLocationItems(playerLocation).getBall();
            getLocationItems(playerLocation).setBall(null);
            getLocationItems(location).setBall(ball);
            ballLocation = location;
        }

        PlayResult result = PlayResult.move(player, location);

        gameLog.add(result.getDisplayString());

        return result;
    }

    /**
     * Returns a collection of actions to be taken to enact the goals set by the
     * players at each location. E.g. if a player has a destination, the action
     * returned for that location could be "move to X on this path".
     *
     * @return a collection of actions
     */
    public Collection<Action> getObjectiveActions() {
        Collection<Action> actions = new ArrayList<>();

        for (Location loc : itemsByLocation.keySet()) {
            LocationItems items = itemsByLocation.get(loc);
            if (items != null && items.getPlayerGameStatus().isPresent()) {
                Location destination = items.getPlayerGameStatus().get().getDestination();
                if (destination != null && !loc.equals(destination)) {
                    List<Location> path = loc.getPath(destination);
                    // TODO: astar search around players
                    // TODO: use diags! this is all manhattan..
                    // This may be complicated
                    //
                    Location step = path.size() == 1 ? destination : path.get(1);
                    actions.add(PlayerAction.move(items.getPlayer().get(), step));
                }
                // else this status should clear
            } else if (items.hasBall()) {
                Ball ball = items.getBall();
//                BallAction.move(ballLocatiokn)
            }
        }

        return actions;
    }

    // TODO: Pass to a location (to be auto-retrieved for now unless a goal)
    // TBD: a "shot" is just a pass to the back of the net
    //
    // TODO: needs a lot of work to incorporate player lines and stats
    // need to calculate trajectory plus obstacles ie if an opponent blocks or intercepts
    public PlayResult pass(Player target) {
        return pass(getLocation(target));
    }

    // TODO: optional shot options, ie. player input on how much energy or finesse to use
    public PlayResult pass(Location targetLocation) {

        // These are getting obsoleted
        double distanceToTarget = targetLocation.getDistance(ballLocation);
        String distanceToTargetStr = String.valueOf(Utils.round(distanceToTarget));

        // Do expect a passer
        Player passer = getLocationItems(ballLocation).getPlayer().get();
        Player target = getLocationItems(targetLocation).getPlayer().orElse(null);

        final AtomicBoolean intercepted = new AtomicBoolean(false);
        final List<Player> interceptingPlayer = new ArrayList<>(1);

        // TBD: the path does NOT include the final destination, perhaps add it?
        // I don't think it's material atm.
        List<Location> path = ballLocation.getPath(targetLocation);

        // Defense radius, better passer means defense has less opportunity
        // Note radius is a little weird for the first spot (where the passer is),
        // the defender can be behind the passer.
        // TODO: this calculation should be made elsewhere, like a "LivePlayer" that knows
        // of the Player and in-game stats like fatigue
        final int radius = (int) Math.max(1L, (3 - Math.round(passer.getPower() + passer.getFinesse())));

        for (Location p : path) {
            p.getNeighbors(radius).stream()
                    .forEach(loc -> {
                        if (!intercepted.get()) {
                            LocationItems locationItems = getLocationItems(loc);
                            if (locationItems != null) {
                                Optional<Player> player = locationItems.getPlayer();
                                if (player.isPresent()) {
                                    if (isOpponent(teamHasPossession(), player.get().getTeam())) {
                                        System.out.println("On path, found opponent " + player.get());
                                        Player opponent = player.get();

                                        double defenseCalc = (opponent.getReach() + opponent.getSpeed() + opponent.getFinesse()) / 2;
                                        System.out.println("Defender " + opponent + " scores " + defenseCalc);
                                        if (defenseCalc > 1.0) {
                                            intercepted.set(true);
                                            interceptingPlayer.add(opponent);
                                        }
                                    }
                                }
                            }
                        }
                    });
        }

        PlayResult result = null;

        Ball ball = getLocationItems(ballLocation).getBall();

        if (!intercepted.get()) {
            Team opponent = getOpponent(teamHasPossession());
            List<Location> goalLocations = getGoalLocations(opponent);
            boolean score = goalLocations.stream()
                    .filter(loc -> loc.equals(targetLocation))
                    .findAny().orElse(null) != null;

            getLocationItems(ballLocation).setBall(null);

            // Score or not:
            // Give to random player, not good. Should have a "set game" method.
            if (score) {
                // Give to random player, probably not good. Should have a "set game" method.
                Set<Player> potentionalPossessor = new HashSet<>(opponent.getPlayers());
                Player player = potentionalPossessor.iterator().next();
                Location afterScorePossessionLocation = getLocation(player);

                getLocationItems(afterScorePossessionLocation).setBall(ball);
                ballLocation = afterScorePossessionLocation;

                result = PlayResult.goal(passer, targetLocation);
            } else {
                getLocationItems(targetLocation).setBall(ball);
                ballLocation = targetLocation;
                result = PlayResult.pass(passer, targetLocation, target);
            }
        } else {
            getLocationItems(ballLocation).setBall(null);

            Player playerWithBall = interceptingPlayer.get(0);
            Location possessionLocation = getLocation(playerWithBall);
            getLocationItems(possessionLocation).setBall(ball);
            ballLocation = possessionLocation;

            result = PlayResult.passDefended(passer, targetLocation, target, playerWithBall);
        }

        gameLog.add(result.getDisplayString());

        return result;
    }

    public void setSidebar(Sidebar sidebar, Optional<Player> player) {
        this.sidebar = Objects.requireNonNull(sidebar);
        this.sidebarPlayer = player.orElse(null);
    }

    // Set tracking status
    // This is almost more of a manager action vs a player one..
    public PlayResult track(Player player, Location location) {
        Location playerLocation = getLocation(player);

        Optional<PlayerGameStatus> playerGameStatus = getLocationItems(playerLocation).getPlayerGameStatus();
        getLocationItems(playerLocation).setPlayerGameStatus(playerGameStatus.get().setDestination(location));

        PlayResult result = PlayResult.track(player, location);

        gameLog.add(result.getDisplayString());

        return result;
    }

    private List<String> createPlayerSidebarInfo(Player player) {
        List<String> playerInfo = new ArrayList<>();

        playerInfo.add("# " + player.getNumber());
        playerInfo.add("Stats:");
        playerInfo.add("Power   " + player.getPower());
        playerInfo.add("Speed   " + player.getSpeed());
        playerInfo.add("Reach   " + player.getReach());
        playerInfo.add("Finesse " + player.getFinesse());

        return playerInfo;
    }

    // from height of 0
    private Set<Location> setPitch(Team team, Formation formation,
            boolean reversed) {

        Iterator<Player> players = team.getPlayers().iterator();

        FormationHelper helper = new FormationHelper(formation, HEIGHT, WIDTH);
        Set<Location> zoneLocations = helper.getPlayerPitchLocations(HEIGHT / 2);

        for (Location loc : zoneLocations) {
            Location trueLocation = loc;
            if (reversed) {
                int reverse = HEIGHT - loc.getY() - 1;
                trueLocation = loc.setY(reverse);
            }

            LocationItems items = getLocationItems(trueLocation);
            Player player = null;
            try {
                player = players.next();
            } catch (Throwable th) {
                th.printStackTrace();
            }
            items.setPlayerGameStatus(new PlayerGameStatus(player));
        }

        return zoneLocations;
    }

    private void fillPitch(Team home, Team away, Formation formation) {

        // The goal "hitbox" is right in front of the goals. This is beause there's
        // an issue with the path finding from positive to negative grid space and
        // this is just easier.
        int startingWidth = (WIDTH / 2) - (GOAL_WIDTH / 2);

        List<Location> homeTeamGoals = new ArrayList<>();
        for (int i = startingWidth; i < startingWidth + GOAL_WIDTH; i++) {
            Location l = Location.create(i, 0);
            homeTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(home, homeTeamGoals);

        List<Location> awayTeamGoals = new ArrayList<>();
        for (int i = startingWidth; i < startingWidth + GOAL_WIDTH; i++) {
            Location l = Location.create(i, HEIGHT - 1);
            awayTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(away, awayTeamGoals);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pitch[x][y] = Location.create(x, y);

                LocationItems locationItems = new LocationItems();
                itemsByLocation.put(pitch[x][y], locationItems);
            }
        }

        Set<Location> homePlayerLocations = setPitch(home, formation, false);

        Location randomPlayerLoc = homePlayerLocations.iterator().next();
        getLocationItems(randomPlayerLoc).setBall(Ball.create(0, 0));
        ballLocation = randomPlayerLoc;

        setPitch(away, formation, true);
    }

    private LocationItems getLocationItems(Location location) {
        return itemsByLocation.get(location);
    }

    private boolean isOpponent(Team team, Team opponent) {
        return !team.equals(opponent);
    }

    @Override
    public String toString() {

        List<String> playerInfo = null;
        if (sidebar == Sidebar.PLAYER_INFO) {
            playerInfo = createPlayerSidebarInfo(sidebarPlayer);
        }

        StringBuilder sb = new StringBuilder();

        sb.append("  ").append(homeTeam.getName()).append("\n");

        for (int x = 0; x < WIDTH * 2; x++) {
            if (x >= (WIDTH - 3) && x < (WIDTH + 3)) {
                sb.append("G");
            } else if (x == (WIDTH - 4) || x == (WIDTH + 3)) {
                sb.append("|");
            } else {
                sb.append("=");
            }
        }

        sb.append("\n");

        for (int y = 0; y < HEIGHT; y++) {

            boolean rowHasBall = false;
            List<String> playerNames = new ArrayList<>();

            for (int x = 0; x < WIDTH; x++) {
                Location location = pitch[x][y];
                LocationItems locationItems = getLocationItems(location);

                if (locationItems.getPlayerGameStatus().isPresent() || locationItems.hasBall()) {

                    if (locationItems.hasBall()) {
                        sb.append("B");
                        rowHasBall = true;
                    } else {
                        sb.append(" ");
                    }

                    if (locationItems.getPlayerGameStatus().isPresent()) {
                        PlayerGameStatus playerGameStatus = locationItems.getPlayerGameStatus().get();
                        Location trackingDest = playerGameStatus.getDestination();

                        Player p = locationItems.getPlayer().get();
                        boolean isTeamA = homeTeam.hasPlayer(p.getNumber());
                        String playerIcon = isTeamA ? "X" : "O";
                        sb.append(playerIcon);

                        String playerName = p.getNumber() + "["
                                + (isTeamA ? homeTeam.getName() : awayTeam.getName()) + "]";
                        if (locationItems.hasBall()) {
                            playerName += " with ball";
                        }
                        if (trackingDest != null) {
                            playerName += " tracking to " + trackingDest;
                        }
                        playerNames.add(playerName);
                    }
                } else {
                    sb.append("..");
                }
            }

            switch (sidebar) {
                case PLAYER_INFO:
                    if (y < playerInfo.size()) {
                        sb.append(" ").append(playerInfo.get(y));
                    }
                    break;
                case TEAMS:
                    // Add side bar information
                    if (rowHasBall) {
                        sb.append(" [BALL] ");
                    }

                    if (!playerNames.isEmpty()) {
                        sb.append(" ")
                                .append(playerNames.stream().collect(Collectors.joining(", ")));
                    }
                    break;
            }

            sb.append("\n");
        }

        for (int x = 0; x < WIDTH * 2; x++) {
            if (x >= (WIDTH - 3) && x < (WIDTH + 3)) {
                sb.append("G");
            } else if (x == (WIDTH - 4) || x == (WIDTH + 3)) {
                sb.append("|");
            } else {
                sb.append("=");
            }
        }

        sb.append("\n");

        sb.append("  ").append(awayTeam.getName()).append("\n");

        // print all vs last few
        for (int x = 0; x < gameLog.size(); x++) {
//        for (int x = Math.max(0, gameLog.size() - 5); x < gameLog.size(); x++) {
            sb.append(gameLog.get(x)).append("\n");
        }

        return sb.toString();
    }
}
