/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.core;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
public class Game {

    private final Team homeTeam;
    private final Team awayTeam;
    private final Pitch pitch;

    public Game(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.pitch = new Pitch(this.homeTeam, this.awayTeam);
    }

    public Team teamHasPossession() {
        return this.pitch.teamHasPossession();
    }

    public Player playerHasPossession() {
        return this.pitch.playerHasPossession();
    }

    public GameActions getAvailable(Team team) {
        GameActions availableGameActions = new GameActions();

        // TODO: stats/zone/etc management
//        for (Player player : team.getPlayers()) {
//            availableGameActions.add(ManagerAction.MANAGER_PLAYER_STATS, player);
//            availableGameActions.add(ManagerAction.MANAGER_PLAYER_ZONE, player);
//        }
//
        for (Player player : team.getPlayers()) {
            if (team.hasPlayer(player)) {

                // Yay, I have the ball!
                if (pitch.hasBall(player)) {

                    for (Location goalLocation : pitch.getGoalLocations(pitch.getOpponent(team))) {
                        availableGameActions.add(PlayerAction.shoot(player, goalLocation));
                    }

                    for (Player teammate : team.getTeammates(player)) {
                        availableGameActions.add(PlayerAction.pass(player, teammate));
                    }
                }

                // TODO: MOVE ..later
            }
        }

        return availableGameActions;
    }

    Pitch getPitch() {
        return pitch;
    }

    // private testing version, just take random available actions
    private GameActions TESTapplyGameActions(GameActions availableGameActions) {

        Map.Entry<ManagerAction, Set<Player>> next = availableGameActions.getManagerActions()
                .entrySet().iterator().next();

        GameActions newActions = new GameActions();

        if (next.getKey() == ManagerAction.CANCEL) {
            pitch.setSidebar(Pitch.Sidebar.TEAMS, Optional.empty());
        } else {
            pitch.setSidebar(Pitch.Sidebar.PLAYER_INFO, Optional.of(next.getValue().iterator().next()));

            // depending on action taken, only certain available actions
            // in this test case we're taking a management action, so only "cancel" is allowed
            newActions.add(ManagerAction.CANCEL, null);
        }

        return newActions;
    }

    public void apply(GameActions gameActions) {

        Set<PlayerAction> playerActions = gameActions.getPlayerActions();
        // Should just be 1 action
        if (playerActions.size() != 1) {
            throw new IllegalStateException("1 action expected");
        }

        PlayerAction action = playerActions.iterator().next();
        Pitch.PlayResult result = null;

        switch (action.getAction()) {
            case PASS:
                Player from = action.getPlayer();
                Player to = action.getTeammate();

                if (!pitch.hasBall(from)) {
                    throw new IllegalStateException("Player doesn't have ball");
                }

                result = pitch.pass(to);
                break;
            case SHOOT:
                // "pass" to goal
                Location goalLocation = action.getLocation();
                result = pitch.pass(goalLocation);
                break;
            default:
                throw new IllegalStateException();
        }

        // TODO: keep score/stats
        System.out.println(result.getDisplayString());

        // TODO: update live game stats/scores
    }

    Team getHomeTeam() {
        return homeTeam;
    }

    Team getAwayTeam() {
        return awayTeam;
    }

    private static void runPassDrill(Game game) {

        Random rand = new Random();

        for (int i = 0; i < 20; i++) {
            runPassDrillStep(game, rand);
        }
    }

    private static void runPassDrillStep(Game game, Random rand) {

        System.out.println(game.getPitch());

        GameActions availableGameActions = game.getAvailable(game.teamHasPossession());

        System.out.println("Available actions:");
        System.out.println(availableGameActions);

        GameActions gameAction = new GameActions();

        // 15% chance of shooting, otherwise pass
        if (rand.nextDouble() < .85) {
            List<PlayerAction> randomPlayerAction = availableGameActions.getPlayerActions().stream()
                    .filter(pa -> pa.getAction() != PlayerAction.Action.SHOOT)
                    .collect(Collectors.toList());
            Collections.shuffle(randomPlayerAction);

            gameAction.add(randomPlayerAction.get(0));
        } else {
            // hardcoded location to shoot at
            Location goalLocation = game.getPitch().getGoalLocations(game.teamHasPossession()).get(3);
            gameAction.add(PlayerAction.shoot(game.playerHasPossession(), goalLocation));
        }

        // apply to game
        game.apply(gameAction);
    }

    private static void runScenarios(Game game) {

        System.out.println(game.getPitch());

        GameActions availableGameActions = game.getAvailable(game.getHomeTeam());
        System.out.println("Available actions:");
        System.out.println(availableGameActions);

        availableGameActions = game.TESTapplyGameActions(availableGameActions);

        System.out.println(game.getPitch());
        System.out.println("Available actions:");
        System.out.println(availableGameActions);

        // should have cancelled
        availableGameActions = game.TESTapplyGameActions(availableGameActions);

        System.out.println(game.getPitch());
        System.out.println("Available actions:");
        System.out.println(availableGameActions);

//        Map<Player, Pair<PlayerAction, Location>> actionsByPlayer = new HashMap<>();
//
//        // Programatically stuffing the available actions back into the game.
//        // Player with ball would shoot or pass, it's likely last one wins right now.
//        availableGameActions.getManagerActions().entrySet().forEach(entry -> {
//            entry.getValue().forEach(player -> {
//                actionsByPlayer.put(player, Pair.create(entry.getKey(), new Location(0, 0)));
//            });
//        });
//
//        GameInput gameInput = new GameInput(actionsByPlayer);
//        System.out.println("Game input:");
//        System.out.println(gameInput.toDisplayString(game));
    }

    // "turns" instead of duration for now
    private static void playVsPc(Game game, String playerTeam, int turns) {

        Random rand = new Random();

        for (int turn = turns; turn > 0; turn--) {
            System.out.println(game.getPitch());
            System.out.println(" Remaining turns: " + turn);
            System.out.println(" Possession: " + game.teamHasPossession().getName());
            System.out.println("\n");

            if (game.teamHasPossession().getName().equals(playerTeam)) {
                GameActions availableGameActions = game.getAvailable(game.teamHasPossession());

                GameActions gameAction = getPlayerAction(availableGameActions);

                game.apply(gameAction);
            } else {
                // AI turn
                runPassDrillStep(game, rand);
            }
        }
    }

    // TODO: shape
    public static GameActions getPlayerAction(GameActions availableGameActions) {

        System.out.println("Available actions:");
//        System.out.println(availableGameActions);
        Map<Integer, PlayerAction> labeledPlayerActions = availableGameActions.getLabeledPlayerActions();

        String inputOptions = labeledPlayerActions.entrySet()
                .stream()
                .map(e -> e.getKey() + "=\"" + e.getValue() + "\"")
                .collect(Collectors.joining("\n"));

        Integer input = null;
        while (input == null) {

            String inputStr = null;

            try {
                inputStr = Utils.getStdIn(Optional.of(inputOptions));

                Integer i = Integer.parseInt(inputStr);
                if (!labeledPlayerActions.containsKey(i)) {
                    System.err.println("Invalid action");
                }
                input = i;
            } catch (IOException io) {
                io.printStackTrace(System.err);
                System.exit(1);
            } catch (NumberFormatException nfe) {
                System.err.println("Unexpected input " + inputStr);
            }
        }

        GameActions playerAction = new GameActions();
        playerAction.add(labeledPlayerActions.get(input));

        return playerAction;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Team teamA = createTeam("USA");
        Team teamB = createTeam("JAPAN");

        Game game = new Game(teamA, teamB);
//        runScenarios(game);
//        runPassDrill(game);
        playVsPc(game, "USA", 20);
    }

    private static Team createTeam(String name) {
        Random rand = new Random();

        Team team = new Team(name);

        for (int i = 0; i < 10; i++) {
            int playerNumber = rand.nextInt(10);
            playerNumber = ((i == 0 ? 1 : i) * 10) + playerNumber;

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
}
