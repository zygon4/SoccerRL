package com.zygon.rl.soccer.core;

import com.zygon.rl.soccer.strategy.FormationHelper;
import com.zygon.rl.soccer.utils.Utils;

import java.util.ArrayList;
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

        private final Player passer;
        private final Location target;
        private final boolean goal;
        private final Player targetPlayer;
        private final Player defender;

        private PlayResult(Player passer, Location target,
                Player targetPlayer, Player defender, boolean goal) {
            this.passer = Objects.requireNonNull(passer);
            this.target = Objects.requireNonNull(target);
            this.targetPlayer = targetPlayer;
            this.defender = defender;
            this.goal = goal;

            if (defender != null && goal) {
                throw new IllegalStateException();
            }
        }

        public static PlayResult pass(Player passer, Location target,
                Player targetPlayer) {
            return new PlayResult(passer, target, targetPlayer, null, false);
        }

        public static PlayResult passDefended(Player passer, Location target,
                Player targetPlayer, Player defender) {
            return new PlayResult(passer, target, targetPlayer, defender, false);
        }

        public static PlayResult goal(Player passer, Location target) {
            return new PlayResult(passer, target, null, null, true);
        }

        public boolean isGoal() {
            return goal;
        }

        public Player getPasser() {
            return passer;
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

            sb.append("#").append(passer.getNumber()).append(" of ").append(passer.getTeam().getName());

            // passes to, shoot at
            if (getTargetPlayer().isPresent()) {
                // passes to
                Player tarPlayer = getTargetPlayer().get();
                sb.append(" passes to #").append(tarPlayer.getNumber());
            } else {
                // TODO: either a "shot" on goal, or "pass" to the open field
                sb.append(" passes to ").append(getTarget());
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
    }

    private static final int HEIGHT = 30; // not including the goal
    private static final int WIDTH = 20;
    private static final int GOAL_WIDTH = 6;

    // TBD: experimental
    public static enum Sidebar {
        PLAYER_INFO,
        TEAMS;
    }

    private final Location[][] pitch = new Location[WIDTH][HEIGHT];
    private final Map<Team, List<Location>> orderedGoalLocationsByTeam = new HashMap<>(2);
    private final Map<Location, LocationItems> itemsByLocation = new HashMap<>();
    private final Team teamA;
    private final Team teamB;
    private final Formation defaultFormation;
    private final List<String> gameLog = new ArrayList<>();
    private final Random rand = new Random();
    private Location ballLocation = null;
    private Sidebar sidebar = Sidebar.TEAMS;
    private Player sidebarPlayer = null; // optional

    public Pitch(Team teamA, Team teamB, Formation defaultFormation) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.defaultFormation = defaultFormation;

        fillPitch(this.teamA, this.teamB, this.defaultFormation);
    }

    public List<String> getGameLog() {
        return Collections.unmodifiableList(gameLog);
    }

    public List<Location> getGoalLocations(Team team) {
        return orderedGoalLocationsByTeam.get(team);
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

    public Team getOpponent(Team team) {
        return team.equals(teamA) ? teamB : teamA;
    }

    public boolean hasBall(Player player) {
        Location playerLocation = getLocation(player);
        return playerLocation.equals(ballLocation);
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
        final long radius = Math.max(1L, (3 - Math.round(passer.getPower() + passer.getFinesse())));

// TBD: log?
//        System.out.println("Path from " + ballLocation + " to " + targetLocation);
//        System.out.println(path.stream()
//                .map(Location::toString)
//                .collect(Collectors.joining(",")));
//
        for (Location p : path) {
            p.getRadius(p, radius).stream()
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

        if (!intercepted.get()) {
            Team opponent = getOpponent(teamHasPossession());
            List<Location> goalLocations = getGoalLocations(opponent);
            boolean score = goalLocations.stream()
                    .filter(loc -> loc.equals(targetLocation))
                    .findAny().orElse(null) != null;

            getLocationItems(ballLocation).setHasBall(false);

            // Score or not:
            // Give to random player, not good. Should have a "set game" method.
//            Set<Player> potentionalPossessor = new HashSet<>(opponent.getPlayers());
//            Player player = potentionalPossessor.iterator().next();
//            Location afterPassPossessionLocation = getLocation(player);
//
//            getLocationItems(afterPassPossessionLocation).setHasBall(true);
//            ballLocation = afterPassPossessionLocation;
            if (score) {
                // Give to random player, probably not good. Should have a "set game" method.
                Set<Player> potentionalPossessor = new HashSet<>(opponent.getPlayers());
                Player player = potentionalPossessor.iterator().next();
                Location afterScorePossessionLocation = getLocation(player);

                getLocationItems(afterScorePossessionLocation).setHasBall(true);
                ballLocation = afterScorePossessionLocation;

                result = PlayResult.goal(passer, targetLocation);
            } else {
                getLocationItems(targetLocation).setHasBall(true);
                ballLocation = targetLocation;
                result = PlayResult.pass(passer, targetLocation, target);
            }
        } else {
            getLocationItems(ballLocation).setHasBall(false);

            Player playerWithBall = interceptingPlayer.get(0);
            Location possessionLocation = getLocation(playerWithBall);
            getLocationItems(possessionLocation).setHasBall(true);
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

    private Set<Location> getSpaced(FormationHelper formationHelper, int count, int minHeight, int maxHeight) {
        Set<Location> locations = new HashSet<>();

        for (int x : formationHelper.getRandomWidths(count)) {
            int randHeight = minHeight + rand.nextInt(maxHeight - minHeight);
            locations.add(new Location(x, randHeight));
        }

        return locations;
    }

    // from height of 0
    private Set<Location> setPitch(Team team, Formation formation, boolean reversed) {

        FormationHelper helper = new FormationHelper(formation, HEIGHT, WIDTH);
        // TODO: should be sorted or organized by position
        Iterator<Player> players = team.getPlayers().iterator();
        List<Integer> playerCountAtZone = formation.getPlayerCountAtZone();

        int zone = 0;
        int zonePlayerCount = playerCountAtZone.get(zone);
        int zoneMin = 0;
        int zoneHeight = helper.getHeight(zone);
        Set<Location> zoneLocations = getSpaced(helper, zonePlayerCount, 0, helper.getHeight(zone));

        for (int y = 0; y < HEIGHT; y++) {

            if (y >= zoneMin + zoneHeight) {
                zone++;
                zoneHeight = helper.getHeight(zone);
                zonePlayerCount = playerCountAtZone.get(zone);
                zoneMin = y;
                zoneLocations.addAll(getSpaced(helper, zonePlayerCount, zoneMin, zoneMin + zoneHeight));
            }
        }

        for (Location loc : zoneLocations) {

            Location trueLocation = loc;
            if (reversed) {
                int reverse = HEIGHT - loc.getY() - 1;
                trueLocation = loc.setY(reverse);
            }

            LocationItems items = getLocationItems(trueLocation);
            items.setPlayer(players.next());
        }

        return zoneLocations;
    }

    private void fillPitch(Team home, Team away, Formation formation) {

        // The goal "hitbox" is right in front of the goals. This is beause there's
        // an issue with the path finding from positive to negative grid space and
        // this is just easier.
        List<Location> homeTeamGoals = new ArrayList<>();
        for (int i = 0; i < GOAL_WIDTH; i++) {
            Location l = new Location(i + 8, 0);
            homeTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(home, homeTeamGoals);

        List<Location> awayTeamGoals = new ArrayList<>();
        for (int i = 0; i < GOAL_WIDTH; i++) {
            Location l = new Location(i + 8, HEIGHT - 1);
            awayTeamGoals.add(l);
        }
        orderedGoalLocationsByTeam.put(away, awayTeamGoals);

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pitch[x][y] = new Location(x, y);

                LocationItems locationItems = new LocationItems();
                itemsByLocation.put(pitch[x][y], locationItems);
            }
        }

        Set<Location> homePlayerLocations = setPitch(home, formation, false);

        Location randomPlayerLoc = homePlayerLocations.iterator().next();
        getLocationItems(randomPlayerLoc).setHasBall(true);
        ballLocation = randomPlayerLoc;

        // TODO: fix this - there is an off-by one
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

        sb.append("  ").append(teamA.getName()).append("\n");

        for (int x = 0; x < WIDTH * 2; x++) {
            if (x >= (WIDTH - 3) && x < (WIDTH + 3)) {
                sb.append("G");
            } else if (x == (WIDTH - 4) || x == (WIDTH + 3)) {
                sb.append("|");
            } else {
                sb.append("-");
            }
        }

        sb.append("\n");

        for (int y = 0; y < HEIGHT; y++) {

            boolean rowHasBall = false;
            List<String> playerNames = new ArrayList<>();

            for (int x = 0; x < WIDTH; x++) {
                Location location = pitch[x][y];
                LocationItems locationItems = getLocationItems(location);

                if (locationItems.getPlayer().isPresent() || locationItems.hasBall()) {

                    if (locationItems.hasBall()) {
                        sb.append("B");
                        rowHasBall = true;
                    } else {
                        sb.append(" ");
                    }

                    if (locationItems.getPlayer().isPresent()) {
                        Player p = locationItems.getPlayer().get();
                        boolean isTeamA = teamA.hasPlayer(p);
                        String playerIcon = isTeamA ? "X" : "O";
                        sb.append(playerIcon);

                        String playerName = p.getNumber() + "["
                                + (isTeamA ? teamA.getName() : teamB.getName()) + "]";
                        if (locationItems.hasBall()) {
                            playerName += ", with ball";
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

        sb.append("  ").append(teamB.getName()).append("\n");

        // print all vs last few
        for (int x = 0; x < gameLog.size(); x++) {
//        for (int x = Math.max(0, gameLog.size() - 5); x < gameLog.size(); x++) {
            sb.append(gameLog.get(x)).append("\n");
        }

        return sb.toString();
    }
}
