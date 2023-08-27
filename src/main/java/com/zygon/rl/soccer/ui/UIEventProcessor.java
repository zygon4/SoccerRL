package com.zygon.rl.soccer.ui;

import com.zygon.rl.soccer.core.Location;
import com.zygon.rl.soccer.core.Player;
import com.zygon.rl.soccer.core.PlayerAction;
import com.zygon.rl.soccer.core.pitch.PlayerEntity;
import com.zygon.rl.soccer.game.Game;
import com.zygon.rl.soccer.utils.Pair;
import org.hexworks.zircon.api.data.Position;
import org.hexworks.zircon.api.uievent.KeyCode;
import org.hexworks.zircon.api.uievent.KeyboardEvent;
import org.hexworks.zircon.api.uievent.MouseEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Collectors;

import static org.hexworks.zircon.api.uievent.KeyCode.ESCAPE;

/**
 *
 * @author zygon
 */
public class UIEventProcessor {

    public static final int LEFT_MOUSE = 1;
    public static final int RIGHT_MOUSE = 3;

    public enum Target {
        PITCH,
        PLAYER,
        GOAL
    }

    public enum NodeFinish {
        CLEAR,
        POP
    }

    /**
     * Represents information to process an input. Holds onto a
     * {@link ActionValue} which has the current data stored for any given node.
     */
    public static final class GameActionNode {

        private final String name;
        private final Set<Target> targets;
        private final int mouseKey;
        private final boolean multi;
        private final Set<GameActionNode> nodes;
        private final NodeFinish finish;
        private final ActionValue value;

        private GameActionNode(String name, Set<Target> targets,
                int mouseKey, boolean multi, Set<GameActionNode> actions,
                NodeFinish finish, ActionValue value) {
            this.name = name;
            this.targets = targets;
            this.mouseKey = mouseKey;
            this.multi = multi;

            Set<GameActionNode> a = actions == null
                    ? new LinkedHashSet<>() : new LinkedHashSet<>(actions);

            // TESTING: adding ourselves here vs outside
            Set<String> byName = a.stream()
                    .map(GameActionNode::getName)
                    .collect(Collectors.toSet());

            if (actions != null && !actions.isEmpty() && !byName.contains(getName())) {
                a.add(this);
            }

            this.nodes = Collections.unmodifiableSet(a);
            this.finish = finish;
            this.value = value;
        }

        public static GameActionNode create(String name, Set<Target> targets,
                int mouseKey) {
            return new GameActionNode(name, targets, mouseKey, false, null, NodeFinish.CLEAR, null);
        }

        public NodeFinish getFinish() {
            return finish;
        }

        public GameActionNode setFinish(NodeFinish finish) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        public String getName() {
            return name;
        }

        public GameActionNode setName(String name) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        public Set<GameActionNode> getNodes() {
            return nodes;
        }

        public GameActionNode setNodes(Set<GameActionNode> nodes) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        public int getInputKey() {
            return mouseKey;
        }

        public GameActionNode setInputKey(int inputKey) {
            return new GameActionNode(name, targets, inputKey, multi, nodes, finish, value);
        }

        /**
         * Returns true if this action has a "multi" concept like a
         * multi-select. This isn't being used yet!
         *
         * @return
         */
        public boolean isMulti() {
            return multi;
        }

        public GameActionNode setIsMulti(boolean multi) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        public Set<Target> getTargets() {
            return targets;
        }

        public GameActionNode setTargets(Set<Target> targets) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        private ActionValue getValue() {
            return value;
        }

        private GameActionNode setValue(ActionValue value) {
            return new GameActionNode(name, targets, mouseKey, multi, nodes, finish, value);
        }

        /**
         * Could use the natural tree-like nature but the values down the tree
         * are not currently set. Values are set and then pushed onto a stack,
         * turned here into a list.
         *
         * @param game
         * @param nodes
         * @return
         */
        public PlayerAction build(Game game, List<GameActionNode> nodes) {

            if (nodes.size() > 2) {
                throw new UnsupportedOperationException(String.valueOf(nodes.size()));
            }

            if (!nodes.isEmpty()) {
                GameActionNode source = nodes.get(0);
                Location sourceLocation = source.getValue().getSourceLocation();
                Player sourcePlayer = source.getValue().getSourcePlayer();

                GameActionNode targetAction = nodes.get(nodes.size() - 1);
                Location targetLocation = targetAction.getValue().getTargetLocation();
                Player targetPlayer = targetAction.getValue().getDestinationPlayer();

                PlayerEntity playerStatus = game.getPlayer(sourceLocation);
                boolean playerHasBall = playerStatus != null && sourceLocation.equals(game.getBall());
                Set<Target> targets = targetAction.getTargets();

                if (targets.contains(Target.GOAL) && game.isGoal(targetLocation) && playerHasBall) {
                    return PlayerAction.shoot(sourcePlayer, targetLocation);
                }

                if (targets.contains(Target.PLAYER) && targetPlayer != null && playerHasBall) {
                    return PlayerAction.pass(sourcePlayer, targetPlayer);
                }

                if (targets.contains(Target.PITCH) && targetLocation != null) {
                    return PlayerAction.track(sourcePlayer, targetLocation);
                }
            }

            return null;
        }

        boolean isValidTarget(Location location, Target target, Game game) {
            switch (target) {
                case GOAL:
                    return game.isGoal(location);
                case PITCH:
                    return !game.isGoal(location);
                case PLAYER:
                    return game.getPlayer(location) != null;
                default:
                    throw new IllegalStateException(target.name());
            }
        }

        boolean isValidTarget(Location location, Game game) {
            // would prefer a streamy way..
            for (Target target : getTargets()) {
                if (isValidTarget(location, target, game)) {
                    return true;
                }
            }

            return false;
        }
    }

    // Bimodal value class for either a source or destination to build up an action
    private static class ActionValue {

        private final Player sourcePlayer;
        private final Location sourceLocation;
        private final Player destinationPlayer;
        private final Location targetLocation;

        private ActionValue(Player sourcePlayer, Location sourceLocation,
                Player destinationPlayer, Location targetLocation) {
            this.sourcePlayer = sourcePlayer;
            this.sourceLocation = sourceLocation;
            this.destinationPlayer = destinationPlayer;
            this.targetLocation = targetLocation;
        }

        public static ActionValue createSource(Player player, Location location) {
            return new ActionValue(player, location, null, null);
        }

        public static ActionValue createDestination(Player player,
                Location location) {
            return new ActionValue(null, null, player, location);
        }

        public Player getDestinationPlayer() {
            return destinationPlayer;
        }

        public Location getSourceLocation() {
            return sourceLocation;
        }

        public Player getSourcePlayer() {
            return sourcePlayer;
        }

        public Location getTargetLocation() {
            return targetLocation;
        }
    }

    /*
    TODO: select -> select, need a way to select a player and then select another player without it being invalid
    TODO: multi-select

        "whats" first, then "hows"
      [quit | player_selected | set aggressiveness ]
        quit -> esc/cancel
        move <- select location (left mouse?)
        shoot <- select location
        pass <- select location

        player_selected -> [ move | shoot | pass | quit ]
     */
// TODO: shot types and argument requirements ie the how
    private static final GameActionNode shoot = GameActionNode.create("shoot", Set.of(Target.GOAL), RIGHT_MOUSE);
    private static final GameActionNode move = GameActionNode.create("move", Set.of(Target.PITCH), RIGHT_MOUSE).setFinish(NodeFinish.POP);
    // should be able to pass to a rando location..
    private static final GameActionNode pass = GameActionNode.create("pass", Set.of(Target.PLAYER), RIGHT_MOUSE).setFinish(NodeFinish.POP);

    private static final GameActionNode select = GameActionNode.create("select",
            Set.of(Target.PLAYER), LEFT_MOUSE)
            .setIsMulti(true)
            .setNodes(Set.of(shoot, move, pass))
            .setFinish(NodeFinish.CLEAR);

    // TODO: all
    private static final Map<Integer, GameActionNode> actionsByMouseKey = Map.of(select.getInputKey(), select);

    private final Set<KeyCode> pressedKey = new HashSet<>();
    private final Queue<UIEvent> queue = new ArrayBlockingQueue<>(1000);
    private final Game game;

    // Stack is the layer of inputs required to perform an action
    private final Stack<GameActionNode> nodes = new Stack<>();

    public UIEventProcessor(Game game) {
        this.game = game;
    }

    public synchronized void add(UIEvent event) {
        queue.add(event);
        poke();
    }

    private void poke() {
        while (!queue.isEmpty()) {

            try {
                if (queue.size() > 1) {
                    // Not an error, just information..
                    System.err.println("Processing multiple events..");
                }

                UIEvent event = queue.remove();

                System.out.println("processor) " + event);

                if (event.keyboardEvent() != null) {
                    KeyboardEvent keyboardEvent = event.keyboardEvent();

                    switch (keyboardEvent.getType()) {
                        case KEY_PRESSED:
                            pressedKey.add(keyboardEvent.getCode());
                            break;
                        case KEY_RELEASED:
                            pressedKey.remove(keyboardEvent.getCode());
                            break;
                    }

                    switch (keyboardEvent.getCode()) {
                        case ESCAPE:
                            nodes.clear();
                            pressedKey.clear();
                            break;
                        case SPACE:
                            game.play();

                            // Continue highlighting, etc.
                            UIAction uiAction = getUIEvent(nodes);
                            if (uiAction != null) {
                                game.apply(uiAction);
                            }
                            break;
                    }

                } else {
                    MouseEvent mouseEvent = event.mouseEvent();
                    switch (mouseEvent.getType()) {
                        case MOUSE_CLICKED:
                            Pair<PlayerAction, UIAction> action = handleEvent(mouseEvent.getButton(), mouseEvent.getPosition(), nodes);
                            if (action != null) {
                                if (nodes.size() >= 2) {
                                    // something wrong..
                                    int a = 1;
                                }

                                if (action.getLeft() != null) {
                                    game.apply(action.getLeft());
                                }

                                if (action.getRight() != null) {
                                    game.apply(action.getRight());
                                }
                            }
                            break;
                        default:
                            System.out.println(mouseEvent);
                            break;
                    }
                }
            } catch (Throwable th) {
                th.printStackTrace(System.err);
            }
        }
    }

    /**
     * Return the action that should be used considering the available actions
     * and conditions at the location and key pressed.
     *
     * @param node
     * @param key
     * @return
     */
    private GameActionNode descentActionTree(GameActionNode node, int key,
            Location chosenLocation) {

        Map<Integer, List<GameActionNode>> availableActionsByKey = node.getNodes().stream()
                .collect(Collectors.groupingBy(GameActionNode::getInputKey));

        List<GameActionNode> possibleActions = availableActionsByKey.get(key);
        if (possibleActions == null) {
            return null;
        }

        // TODO: when is it okay to return the same action? e.g. 'selectPlayer' and then selected another player..
        //
        //
        // if target is goal and loc is goal, good
        // if target is player and loc has player, good
        // if target is pitch and loc is !goal, good
        // So these are the possible options, this feels a little flimsy..
        return possibleActions.stream().sorted((o1, o2) -> {
            int o1Sort = o1.getTargets().contains(Target.GOAL) && game.isGoal(chosenLocation) ? 10 : 0;
            o1Sort += o1.getTargets().contains(Target.PLAYER) && game.getPlayer(chosenLocation) != null ? 5 : 0;
            o1Sort += o1.getTargets().contains(Target.PITCH) && !game.isGoal(chosenLocation) ? 1 : 0;

            int o2Sort = o2.getTargets().contains(Target.GOAL) && game.isGoal(chosenLocation) ? 10 : 0;
            o2Sort += o2.getTargets().contains(Target.PLAYER) && game.getPlayer(chosenLocation) != null ? 5 : 0;
            o2Sort += o2.getTargets().contains(Target.PITCH) && !game.isGoal(chosenLocation) ? 1 : 0;

            return o1Sort > o2Sort ? -1 : o1Sort < o2Sort ? 1 : 0;
        }).findFirst().orElse(null);
    }

    private UIAction getUIEvent(List<GameActionNode> nodes) {

        if (nodes.size() > 1) {

            // This code block is being re-used, should refactor.
            GameActionNode source = nodes.get(0);
            Location sourceLocation = source.getValue().getSourceLocation();

            GameActionNode targetAction = nodes.get(nodes.size() - 1);
            Location targetLocation = targetAction.getValue().getTargetLocation();
            Player targetPlayer = targetAction.getValue().getDestinationPlayer();

            if (targetPlayer != null) {
                return UIAction.highlightPath(sourceLocation, targetLocation);
            }
        } else if (!nodes.isEmpty()) {
            return UIAction.highlightPlayer(nodes.get(0).getValue().getSourcePlayer());
        }

        return null;
    }

    /**
     * Primary handler for mouse buttons. Builds the PlayerAction with each
     * input.
     *
     * @param mouseButton
     * @param position
     * @param nodes
     * @return
     */
    private Pair<PlayerAction, UIAction> handleEvent(int mouseButton,
            Position position, Stack<GameActionNode> nodes) {

        final Location location = UserInterface.fromTileGridToPitch(position);

        if (location == null) {
            return null;
        }

        GameActionNode availableAction = null;

        if (nodes.isEmpty()) {
            // What actions are available at the top level?
            availableAction = actionsByMouseKey.get(mouseButton);
        } else {
            availableAction = descentActionTree(nodes.get(nodes.size() - 1), mouseButton, location);
        }

        if (availableAction == null || !availableAction.isValidTarget(location, game)) {
            System.out.println("Invalid target..");
        } else {
            // Exact same action.. pop
            if (!nodes.isEmpty() && nodes.peek().getName().equals(availableAction.getName())) {
                nodes.pop();
            }

            PlayerEntity playerStatus = game.getPlayer(location);
            Player player = playerStatus != null ? playerStatus.getPlayer() : null;

            if (nodes.isEmpty()) {
                availableAction = availableAction.setValue(ActionValue.createSource(player, location));
            } else {
                availableAction = availableAction.setValue(ActionValue.createDestination(player, location));
            }

            nodes.add(availableAction);
        }

        PlayerAction action = availableAction != null
                ? availableAction.build(game, nodes.stream().collect(Collectors.toList()))
                : null;
        UIAction uiAction = getUIEvent(nodes);

        if (action != null) {
            // After an action is built, we may want to clear the nodes, we may want to keep it.
            // e.g. keep moving a player
            switch (availableAction.getFinish()) {
                case CLEAR:
                    nodes.clear();
                    break;
                case POP:
                    nodes.pop();
                    break;
                default:
                    throw new UnsupportedOperationException(availableAction.getFinish().name());
            }
        }

        return Pair.create(action, uiAction);
    }
}
