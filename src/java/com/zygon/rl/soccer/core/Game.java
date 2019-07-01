/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author zygon
 */
public class Game {

    private final Team teamA;
    private final Team teamB;
    private final Pitch pitch;

    public Game(Team teamA, Team teamB) {
        this.teamA = teamA;
        this.teamB = teamB;
        this.pitch = new Pitch(this.teamA, this.teamB);
    }

    public Team teamHasPossession() {
        return this.pitch.teamHasPossession();
    }

    public Player playerHasPossession() {
        return this.pitch.playerHasPossession();
    }

    public GameActions getAvailable(Team team) {
        GameActions availableGameActions = new GameActions();

        for (Player player : team.getPlayers()) {
            availableGameActions.add(ManagerAction.MANAGER_PLAYER_STATS, player);
            availableGameActions.add(ManagerAction.MANAGER_PLAYER_ZONE, player);
        }

        for (Player player : team.getPlayers()) {
            if (team.hasPlayer(player)) {

                // Yay, I have the ball!
                if (pitch.hasBall(player)) {
                    availableGameActions.addShootAction(player);

                    for (Player teammate : team.getTeammates(player)) {
                        availableGameActions.addPassAction(player, teammate);
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

        Player shooter = gameActions.getShooter();

        if (shooter != null) {
            // shoot!
            if (pitch.shoot()) {
                System.out.println("GOAAAAAALLLLL");
            }
        } else {
            // Should only be one, if more than one just grab the first
            Pair<Player, Player> passFromTo = gameActions.getPassFromTo().iterator().next();

            if (!pitch.hasBall(passFromTo.getLeft())) {
                throw new IllegalStateException("Player doesn't have ball");
            }

            pitch.pass(passFromTo.getRight());
        }
        // TODO: the fun parts
    }

    Team getTeamA() {
        return teamA;
    }

    Team getTeamB() {
        return teamB;
    }

    private static void runPassDrill(Game game) {

        Random rand = new Random();

        for (int i = 0; i < 20; i++) {
            System.out.println(game.getPitch());

            GameActions availableGameActions = game.getAvailable(game.teamHasPossession());

            System.out.println("Available actions:");
            System.out.println(availableGameActions);

            GameActions gameAction = new GameActions();

            if (rand.nextDouble() < .85) {
                // get pass action
                Pair<Player, Player> passPair = new HashSet<>(
                        availableGameActions.getPassFromTo()).iterator().next();
                gameAction.addPassAction(passPair.getLeft(), passPair.getRight());
            } else {
                gameAction.addShootAction(game.playerHasPossession());
            }

            // apply to game
            game.apply(gameAction);
        }
    }

    private static void runScenarios(Game game) {

        System.out.println(game.getPitch());

        GameActions availableGameActions = game.getAvailable(game.getTeamA());
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Team teamA = createTeam("Team A");
        Team teamB = createTeam("Team B");

        Game game = new Game(teamA, teamB);
//        runScenarios(game);
        runPassDrill(game);
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

        return Math.round(val * 100) / 100D;
    }
}
