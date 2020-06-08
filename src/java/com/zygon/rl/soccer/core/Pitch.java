package com.zygon.rl.soccer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

    private static final int HEIGHT = 30;
    private static final int WIDTH = 20;

    // TBD: experimental
    public static enum Sidebar {
        PLAYER_INFO,
        TEAMS;
    }

    private final Location[][] pitch = new Location[WIDTH][HEIGHT];
    private final Map<Location, LocationItems> itemsByLocation = new HashMap<>();
    private final Team teamA;
    private final Team teamB;
    private final List<String> gameLog = new ArrayList<>();
    private final Random rand = new Random();
    private Location ballLocation = null;
    private Sidebar sidebar = Sidebar.TEAMS;
    private Player sidebarPlayer = null; // optional

    public Pitch(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;

        fillPitch(pitch, this.teamA, this.teamB);
    }

    public List<String> getGameLog() {
        return Collections.unmodifiableList(gameLog);
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

    public boolean hasBall(Player player) {
        Location playerLocation = getLocation(player);
        return playerLocation.equals(ballLocation);
    }

    // TODO: needs a lot of work to incorporate player lines and stats
    // need to calculate trajectory plus obstacles ie if an opponent blocks or intercepts
    public void pass(Player target) {
        pass(getLocation(target));
    }

    public void pass(Location targetLocation) {

        // These are getting obsoleted
        double distanceToTarget = targetLocation.getDistance(ballLocation);
        String distanceToTargetStr = String.valueOf(Utils.round(distanceToTarget));

        // Do expect a passer
        Player passer = getLocationItems(ballLocation).getPlayer().get();
        Player target = getLocationItems(targetLocation).getPlayer().get();

        final AtomicBoolean intercepted = new AtomicBoolean(false);
        final List<Player> interceptingPlayer = new ArrayList<>(1);

        // TBD: the path does NOT include the final destination, perhaps add it?
        // I don't think it's material atm.
        List<Location> path = ballLocation.getPath(targetLocation);

        // Defense radius, better passer means defense has less opportunity
        // Note radius is a little weird for the first spot (where the passer is),
        // the defender can be behind the passer.
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

        if (!intercepted.get()) {
            getLocationItems(ballLocation).setHasBall(false);
            getLocationItems(targetLocation).setHasBall(true);

            gameLog.add("#" + passer.getNumber() + " of " + teamHasPossession().getName()
                    + " passes to #" + target.getNumber() + " (" + distanceToTargetStr + ")");

            // TODO: game log w/ alternative outcomes
            // For now just 100% success
            ballLocation = targetLocation;
        } else {
            getLocationItems(ballLocation).setHasBall(false);
            Team hasPossession = teamHasPossession();
            Team gainsPossession = getOpponent(hasPossession);

            Player playerWithBall = interceptingPlayer.get(0);

            Location possessionLocation = getLocation(playerWithBall);
            getLocationItems(possessionLocation).setHasBall(true);

            gameLog.add("#" + passer.getNumber() + " of team " + teamHasPossession().getName()
                    + " pass to #" + target.getNumber() + " (" + distanceToTargetStr + ") intercepted by #"
                    + playerWithBall.getNumber() + " of " + gainsPossession.getName());

            ballLocation = possessionLocation;
        }
    }

    // TODO: take a location
    public boolean shoot() {

        boolean score = false;

        // score
        // TODO: LOS to goal and defenders before possible block
        if (rand.nextDouble() > 0.80) {
            getLocationItems(ballLocation).setHasBall(false);

            gameLog.add("#" + getLocationItems(ballLocation).getPlayer().get().getNumber()
                    + " GOAL!");

            // For now just pick random player from other team
            Team hasPossession = teamHasPossession();
            Team opposingTeam = getOpponent(hasPossession);

            Set<Player> potentionalPossessor = new HashSet<>(opposingTeam.getPlayers());
            Player player = potentionalPossessor.iterator().next();

            Location afterScorePossessionLocation = getLocation(player);
            getLocationItems(ballLocation).setHasBall(false);
            // could be the same location
            getLocationItems(afterScorePossessionLocation).setHasBall(true);
            ballLocation = afterScorePossessionLocation;

            score = true;
        } else {
            // block, ball goes randomly near the goal
            Team possessingTeam = teamHasPossession();
            Team opposingTeam = getOpponent(possessingTeam);

            Set<Player> potentionalPossessor = new HashSet<>(possessingTeam.getPlayers());
            potentionalPossessor.addAll(opposingTeam.getPlayers());
            Player player = potentionalPossessor.iterator().next();

            Location afterBlockPossessionLocation = getLocation(player);
            getLocationItems(ballLocation).setHasBall(false);
            // could be the same location
            getLocationItems(afterBlockPossessionLocation).setHasBall(true);

            gameLog.add("#" + getLocationItems(ballLocation).getPlayer().get().getNumber()
                    + " shot blocked, recovered by #" + player.getNumber() + " of " + player.getTeam().getName());

            ballLocation = afterBlockPossessionLocation;
        }

        return score;
    }

    void setSidebar(Sidebar sidebar, Optional<Player> player) {
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

    // TBD: with a player squad/formation guide
    private void fillPitch(Location[][] pitch, Team teamA, Team teamB) {

        boolean hasBall = false;

        List<Player> teamAplayers = new ArrayList<>(teamA.getPlayers());
        int teamAPlayerIndex = 0;

        List<Player> teamBplayers = new ArrayList<>(teamB.getPlayers());
        int teamBPlayerIndex = 0;

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                pitch[x][y] = new Location(x, y);

                LocationItems locationItems = null;
                if (teamAPlayerIndex < teamAplayers.size() || teamBPlayerIndex < teamBplayers.size()) {
                    if (rand.nextDouble() > 0.96) {
                        locationItems = new LocationItems();
                        Player player = null;

                        // Total garbage player positioning
                        if (rand.nextBoolean()) {
                            if (teamAPlayerIndex < teamAplayers.size()) {
                                player = teamAplayers.get(teamAPlayerIndex++);
                            } else if (teamBPlayerIndex < teamBplayers.size()) {
                                player = teamBplayers.get(teamBPlayerIndex++);
                            }
                        } else {
                            if (teamBPlayerIndex < teamBplayers.size()) {
                                player = teamBplayers.get(teamBPlayerIndex++);
                            } else if (teamAPlayerIndex < teamAplayers.size()) {
                                player = teamAplayers.get(teamAPlayerIndex++);
                            }
                        }

                        if (!hasBall) {
                            locationItems.setHasBall(true);
                            hasBall = true;
                            ballLocation = pitch[x][y];
                        }

                        locationItems.setPlayer(player);
                    }
                }

                itemsByLocation.put(pitch[x][y], locationItems);
            }
        }

        if (teamAPlayerIndex < teamAplayers.size() || teamBPlayerIndex < teamBplayers.size()) {
            throw new RuntimeException("More players to add " + teamAPlayerIndex + ", " + teamBPlayerIndex);
        }

        if (!hasBall) {
            throw new RuntimeException("No ball");
        }
    }

    private LocationItems getLocationItems(Location location) {
        return itemsByLocation.get(location);
    }

    private Team getOpponent(Team team) {
        return team.equals(teamA) ? teamB : teamA;
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

                if (locationItems != null) {
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
            } else {
                sb.append("-");
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
