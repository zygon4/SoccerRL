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
import java.util.concurrent.ThreadLocalRandom;
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

        Location targetLocation = getLocation(target);

        // These are getting obsoleted
        double distanceToTarget = targetLocation.getDistance(ballLocation);
        String distanceToTargetStr = String.valueOf(Utils.round(distanceToTarget));

        List<Location> path = ballLocation.getPath(targetLocation);

        // check for neighboring players and calculate intercept chance
        Map<Location, Set<Player>> playersByLocation = new HashMap<>();

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Location location = pitch[x][y];
                LocationItems locationItems = getLocationItems(location);

                if (locationItems != null && locationItems.getPlayer().isPresent()) {
                    Player playerOnField = locationItems.getPlayer().get();

                    if (isOpponent(teamHasPossession(), playerOnField.getTeam())) {
                        // TODO: radius based on player stats
                        location.getRadius(location, 5).stream()
                                .forEach(loc -> {
                                    Set<Player> playersInfluencingLocation = playersByLocation.get(loc);
                                    if (playersInfluencingLocation == null) {
                                        playersInfluencingLocation = new HashSet<>();
                                        playersByLocation.put(loc, playersInfluencingLocation);
                                    }
                                    playersInfluencingLocation.add(playerOnField);
                                });
                    }
                }
            }
        }

        boolean intercepted = false;

        for (Location l : path) {
            // On the path, check if there are defenders in the area
            Set<Player> defendingPlayers = playersByLocation.get(l);
            if (defendingPlayers != null) {
                // TODO: weighted random check
                double interceptChance = defendingPlayers.stream()
                        .mapToDouble(player -> {
                            // finesse as pure modifier?
                            // TODO: probably need a few "calculate checks" util functions
                            double calc = (player.getReach() + player.getSpeed() + player.getFinesse()) / 2;
                            System.out.println("Defender " + player + " scores " + calc);
                            return calc;
                        })
                        .max().orElse(0.0);

                // not well normalized, arbitrary chance check
                // TODO: needs work!
                intercepted = interceptChance > 1.0;
            }
        }

        if (!intercepted) {
            getLocationItems(ballLocation).setHasBall(false);

            getLocationItems(targetLocation).setHasBall(true);

            gameLog.add("#" + getLocationItems(ballLocation).getPlayer().get().getNumber()
                    + " of " + teamHasPossession().getName() + " passes to #"
                    + target.getNumber() + " (" + distanceToTargetStr + ")");

            // TODO: game log w/ alternative outcomes
            // For now just 100% success
            ballLocation = targetLocation;
        } else {
            // WIP lose posession
            getLocationItems(ballLocation).setHasBall(false);
            Team hasPossession = teamHasPossession();
            Team gainsPossession = getOpponent(hasPossession);

            Player playerWithBall = gainsPossession.getPlayers().stream()
                    .sorted((o1, o2) -> ThreadLocalRandom.current().nextInt(-1, 2))
                    .findAny().orElse(null);

            Location possessionLocation = getLocation(playerWithBall);
            getLocationItems(possessionLocation).setHasBall(true);

            gameLog.add("#" + getLocationItems(ballLocation).getPlayer().get().getNumber()
                    + " of team " + teamHasPossession().getName() + " pass to #"
                    + target.getNumber() + " (" + distanceToTargetStr + ") intercepted by #"
                    + playerWithBall.getNumber() + " of " + gainsPossession.getName());

            ballLocation = possessionLocation;
        }
    }

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
            Team hasPossession = teamHasPossession();
            Team opposingTeam = getOpponent(hasPossession);

            Set<Player> potentionalPossessor = new HashSet<>(hasPossession.getPlayers());
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
