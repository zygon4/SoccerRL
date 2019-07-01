package com.zygon.rl.soccer.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
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
    private final Team teamA;
    private final Team teamB;
    private final List<String> gameLog = new ArrayList<>();
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
        return ballLocation.getLocationItems().getPlayer().get().getTeam();
    }

    public Player playerHasPossession() {
        return ballLocation.getLocationItems().getPlayer().get();
    }

    public Location getLocation(Player player) {

        // double for loop :(
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                LocationItems locationItems = pitch[x][y].getLocationItems();
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

    private final Random rand = new Random();

    // TODO: needs a lot of work to incorporate player lines and stats
    // need to calculate trajectory plus obstacles ie if an opponent blocks or intercepts
    public void pass(Player target) {

        if (rand.nextDouble() > 0.66) {
            ballLocation.getLocationItems().setHasBall(false);

            Location targetLocation = getLocation(target);
            targetLocation.getLocationItems().setHasBall(true);

            gameLog.add("#" + ballLocation.getLocationItems().getPlayer().get().getNumber()
                    + " passes to #" + target.getNumber());

            // TODO: game log w/ alternative outcomes
            // For now just 100% success
            ballLocation = targetLocation;
        } else {
            // WIP lose posession
            ballLocation.getLocationItems().setHasBall(false);
            Team hasPossession = teamHasPossession();
            Team gainsPossession = getOpponent(hasPossession);

            Player playerWithBall = new HashSet<>(gainsPossession.getPlayers()).iterator().next();

            Location possessionLocation = getLocation(playerWithBall);
            possessionLocation.getLocationItems().setHasBall(true);

            gameLog.add("#" + ballLocation.getLocationItems().getPlayer().get().getNumber()
                    + " pass to #" + target.getNumber() + " intercepted by #"
                    + playerWithBall.getNumber() + " of " + gainsPossession.getName());

            ballLocation = possessionLocation;
        }
    }

    public boolean shoot() {

        boolean score = false;

        // score
        if (rand.nextDouble() > 0.80) {
            ballLocation.getLocationItems().setHasBall(false);

            gameLog.add("#" + ballLocation.getLocationItems().getPlayer().get().getNumber()
                    + " GOAL!");

            // For now just pick random player from other team
            Team hasPossession = teamHasPossession();
            Team opposingTeam = getOpponent(hasPossession);

            Set<Player> potentionalPossessor = new HashSet<>(opposingTeam.getPlayers());
            Player player = potentionalPossessor.iterator().next();

            Location afterScorePossessionLocation = getLocation(player);
            ballLocation.getLocationItems().setHasBall(false);
            // could be the same location
            afterScorePossessionLocation.getLocationItems().setHasBall(true);
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
            ballLocation.getLocationItems().setHasBall(false);
            // could be the same location
            afterBlockPossessionLocation.getLocationItems().setHasBall(true);

            gameLog.add("#" + ballLocation.getLocationItems().getPlayer().get().getNumber()
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

        Random rand = new Random();
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

                        if (teamAPlayerIndex < teamAplayers.size()) {
                            player = teamAplayers.get(teamAPlayerIndex++);
                        } else if (teamBPlayerIndex < teamBplayers.size()) {
                            player = teamBplayers.get(teamBPlayerIndex++);
                        }

                        if (!hasBall) {
                            locationItems.setHasBall(true);
                            hasBall = true;
                            ballLocation = pitch[x][y];
                        }

                        locationItems.setPlayer(player);
                    }
                }

                pitch[x][y].setLocationItems(locationItems);
            }
        }

        if (teamAPlayerIndex < teamAplayers.size() || teamBPlayerIndex < teamBplayers.size()) {
            throw new RuntimeException("More players to add " + teamAPlayerIndex + ", " + teamBPlayerIndex);
        }

        if (!hasBall) {
            throw new RuntimeException("No ball");
        }
    }

    private Team getOpponent(Team team) {
        return team.equals(teamA) ? teamB : teamA;
    }

    @Override
    public String toString() {

        List<String> playerInfo = null;
        if (sidebar == Sidebar.PLAYER_INFO) {
            playerInfo = createPlayerSidebarInfo(sidebarPlayer);
        }

        StringBuilder sb = new StringBuilder();

        for (int x = 0; x < WIDTH * 2; x++) {
            sb.append("-");
        }

        sb.append("\n");

        for (int y = 0; y < HEIGHT; y++) {

            boolean rowHasBall = false;
            List<String> playerNames = new ArrayList<>();

            for (int x = 0; x < WIDTH; x++) {
                Location location = pitch[x][y];
                LocationItems locationItems = location.getLocationItems();

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
            sb.append("-");
        }

        sb.append("\n");

        // print all vs last few
        for (int x = 0; x < gameLog.size(); x++) {
//        for (int x = Math.max(0, gameLog.size() - 5); x < gameLog.size(); x++) {
            sb.append(gameLog.get(x)).append("\n");
        }

        return sb.toString();
    }
}
