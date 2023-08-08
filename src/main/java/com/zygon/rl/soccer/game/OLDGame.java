/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.Formation;
import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.LocationItems;
import com.zygon.rl.soccer.core.Pitch;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.Team;
import com.zygon.rl.soccer.utils.Utils;

import java.awt.Color;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * @author zygon
 */
@Deprecated
public class OLDGame {

    private final Team homeTeam;
    private final Team awayTeam;
    private final Pitch pitch;
    private final Map<Team, Score> scoresByTeam = new HashMap<>();

    public OLDGame(Team homeTeam, Team awayTeam) {
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.pitch = new Pitch(this.homeTeam, this.awayTeam);

        scoresByTeam.put(homeTeam, new Score(homeTeam));
        scoresByTeam.put(awayTeam, new Score(awayTeam));
    }

    public Score get(Team team) {
        return scoresByTeam.get(team);
    }

    public Team teamHasPossession() {
        return this.pitch.teamHasPossession();
    }

    public Team teamNoPossession() {
        return this.pitch.getOpponent(teamHasPossession());
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
        // TODO: other manager actions
//        for (Formation formation : Formations.FORMATIONS) {
//            availableGameActions.add(ManagerAction.setFormation(formation));
//        }
        availableGameActions.add(HumanAction.PLAY_VS_PC);
        availableGameActions.add(HumanAction.QUIT);

        for (Player player : team.getPlayers()) {
            //TBD: why double check?
            if (team.hasPlayer(player.getNumber())) {

                // Yay, I have the ball!
                if (pitch.hasBall(player)) {

                    // shooting options
                    for (Location goalLocation : pitch.getGoalLocations(pitch.getOpponent(team))) {
                        availableGameActions.add(PlayerAction.shoot(player, goalLocation));
                    }

                    // passing options
                    for (Player teammate : team.getTeammates(player)) {
                        availableGameActions.add(PlayerAction.pass(player, teammate));
                    }
                }

                // Direct movement options
//                for (Location move : pitch.getLegalMoves(player)) {
//                    availableGameActions.add(PlayerAction.move(player, move));
//                }
//
                // Tracking movement
                availableGameActions.add(PlayerAction.track(player));
            }
        }

        return availableGameActions;
    }

    Pitch getPitch() {
        return pitch;
    }

    public Map<Location, LocationItems> getItemsByLocation() {
        return pitch.getItemsByLocation();
    }

    public void apply(Action action, String argument) {
        // Sad casting :(
        if (action instanceof HumanAction) {
            apply((HumanAction) action);
        } else if (action instanceof PlayerAction) {
            apply((PlayerAction) action, argument);
        } else if (action instanceof ManagerAction) {
            apply((ManagerAction) action);
        }
    }

    public void apply(HumanAction humanAction) {

        switch (humanAction.getAction()) {
            case QUIT:
                // TODO: confirmation context
                System.exit(0);

            case PLAY_VS_PC:
                playVsPc(this, homeTeam.getName(), 20);
                break;
        }
    }

    public void apply(ManagerAction managerAction) {

        switch (managerAction.getAction()) {
            case MANAGER_TEAM_FORMATION:
                Formation formation = managerAction.getFormation();

                // TODO: pitch.set(stuff)
                break;
        }
    }

    public void apply(PlayerAction playerAction, String argument) {

        // TODO: port this..
        Score score = get(teamHasPossession());
        Pitch.PlayResult result = null;

        switch (playerAction.getAction()) {
            case MOVE:
                result = pitch.move(playerAction.getPlayer(), playerAction.getLocation());
                break;
            case PASS:
                Player from = playerAction.getPlayer();
                Player to = playerAction.getTeammate();

                if (!pitch.hasBall(from)) {
                    throw new IllegalStateException("Player doesn't have ball");
                }

                result = pitch.pass(to);
                break;
            case SHOOT:
                // "pass" to goal
                Location goalLocation = playerAction.getLocation();
                result = pitch.pass(goalLocation);

                if (!result.isGoal()) {
                    score.addSOG();
                }
                break;
            case TRACK:
                // TODO this is broken
                Location destination = null;// Location.parse(argument);
                result = pitch.track(playerAction.getPlayer(), destination);
                break;
            default:
                throw new IllegalStateException();
        }

        // Resulted in a goal
        if (result.isGoal()) {
            score.addGoal();
        }

        // Kept possession
        if (result.getDefendingPlayer().isEmpty()) {
            score.addTimeOfPossession();
        }

        // TODO: keep score/stats
        System.out.println(result.getDisplayString());

        System.out.println(score.getDisplayString());

        // TODO: update live game stats/scores
    }

    Team getHomeTeam() {
        return homeTeam;
    }

    Team getAwayTeam() {
        return awayTeam;
    }

    private static void runPassDrillStep(OLDGame game, Random rand) {

        System.out.println(game.getPitch());

        GameActions availableGameActions = game.getAvailable(game.teamHasPossession());

        System.out.println("Available actions:");
        System.out.println(availableGameActions);

        PlayerAction playerAction = null;

        // 15% chance of shooting, otherwise pass
        if (rand.nextDouble() < .85) {
            List<PlayerAction> randomPlayerAction = availableGameActions.getPlayerActions().stream()
                    .filter(pa -> pa.getAction() != PlayerAction.Action.SHOOT && pa.getAction() != PlayerAction.Action.TRACK)
                    .collect(Collectors.toList());
            Collections.shuffle(randomPlayerAction);

            playerAction = randomPlayerAction.get(0);
        } else {
            // hardcoded location to shoot at
            Location goalLocation = game.getPitch().getGoalLocations(game.getPitch().defendingTeam()).get(3);
            playerAction = PlayerAction.shoot(game.playerHasPossession(), goalLocation);
        }

        // apply to game
        game.apply(playerAction, null);
    }

    // "turns" instead of duration for now
    public static void playVsPc(OLDGame game, String playerTeam, int turns) {

        Random rand = new Random();

        for (int turn = turns; turn > 0; turn--) {
            System.out.println(game.getPitch());
            System.out.println(" Remaining turns: " + turn);
            System.out.println(" Possession: " + game.teamHasPossession().getName());
            System.out.println("\n");

            // TODO: check if players are tracking (ie moving) and move them all
            // If they reach their destination, remove the tracking flag from PlayerGameStatus
            //
            Collection<Action> asyncActions = game.getPitch().getObjectiveActions();
            for (Action action : asyncActions) {
                game.apply(action, null);
            }

            if (game.teamHasPossession().getName().equals(playerTeam)) {
                GameActions availableGameActions = game.getAvailable(game.teamHasPossession());

                Action action = getAction(availableGameActions);

                String argument = null;
                if (action.hasArgument()) {
                    argument = getInput(action.getArgumentPrompt(), (String i) -> {
                        String error = action.getArgumentError(i);
                        if (error != null) {
                            return error;
                        }

                        return null;
                    }, (String i) -> i);
                }

                game.apply(action, argument);
            } else {
                // AI turn
                runPassDrillStep(game, rand);
            }
        }
    }

    public static Action getAction(GameActions availableGameActions) {

        System.out.println("Available actions:");
        Map<Integer, UUID> labeledPlayerActions = availableGameActions.getLabeledPlayerActions();

        String inputOptions = labeledPlayerActions.entrySet()
                .stream()
                .map(e -> e.getKey() + "=\"" + availableGameActions.get(e.getValue()).getDisplayString() + "\"")
                .collect(Collectors.joining("\n"));

        Integer input = getInput(inputOptions,
                (String i) -> {
                    try {
                        int actionNumber = Integer.parseInt(i);
                        if (!labeledPlayerActions.keySet().contains(actionNumber)) {
                            return "action not found";
                        }
                    } catch (NumberFormatException nfe) {
                        return "Unexpected input " + i;
                    }
                    return null;
                },
                (String i) -> Integer.parseInt(i));

        return availableGameActions.get(labeledPlayerActions.get(input));
    }

    // Checks 'validInput' if not null
    private static <T> T getInput(String prompt,
            Function<String, String> validateInput,
            Function<String, T> convert) {

        T input = null;
        while (input == null) {

            String inputStr = null;

            try {
                inputStr = Utils.getStdIn(Optional.of(prompt));
                String inputError = validateInput.apply(inputStr);

                if (inputError != null) {
                    System.err.println("Invalid action: " + inputError);
                } else {
                    T i = convert.apply(inputStr);
                    input = i;
                }
            } catch (IOException io) {
                io.printStackTrace(System.err);
                System.exit(1);
            }
        }

        return input;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Team teamA = createTeam("USA", Color.MAGENTA);
        Team teamB = createTeam("JAPAN", Color.CYAN);

        OLDGame game = new OLDGame(teamA, teamB);
        playVsPc(game, "USA", 20);
    }

    public static Team createTeam(String name, Color color) {
        Random rand = new Random();

        Team team = new Team(name, color);

        // TODO: add keeper
        for (int i = 0; i < 10; i++) {

            int playerNumber = -1;
            do {
                playerNumber = rand.nextInt(10);
                playerNumber = ((i == 0 ? 1 : i) * 10) + playerNumber;
            } while (!team.hasPlayer(playerNumber));

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
