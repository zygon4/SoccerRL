package com.zygon.rl.soccer.game;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.PlayerAction;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 * @author zygon
 */
public class GameActions {

    private final Set<HumanAction> humanActions = new LinkedHashSet<>();
    private final Set<ManagerAction> managerActions = new LinkedHashSet<>();
    private final Set<PlayerAction> playerActions = new LinkedHashSet<>();
    private final Map<UUID, Action> actionsByUuid = new LinkedHashMap<>();

    void add(HumanAction humanAction) {
        add(humanAction.getUuid(), humanAction);
        humanActions.add(humanAction);
    }

    void add(PlayerAction playerAction) {
        add(playerAction.getUuid(), playerAction);
        playerActions.add(playerAction);
    }

    void add(ManagerAction managerAction) {
        add(managerAction.getUuid(), managerAction);
        managerActions.add(managerAction);
    }

    public Set<HumanAction> getHumanActions() {
        return Collections.unmodifiableSet(humanActions);
    }

    public Set<ManagerAction> getManagerActions() {
        return Collections.unmodifiableSet(managerActions);
    }

    public Set<PlayerAction> getPlayerActions() {
        return Collections.unmodifiableSet(playerActions);
    }

    public Action get(UUID uuid) {
        return actionsByUuid.get(uuid);
    }

    // TODO: move this to Game, with UUIDs it's no longer necessary here
    public Map<Integer, UUID> getLabeledPlayerActions() {
        Map<Integer, UUID> labeledActions = new LinkedHashMap<>();

        int index = 1;

        for (UUID action : actionsByUuid.keySet()) {
            labeledActions.put(index++, actionsByUuid.get(action).getUuid());
        }

        return labeledActions;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        for (Action playerAction : playerActions) {
            sb.append(playerAction).append("\n");
        }

        return sb.toString();
    }

    private void add(UUID uuid, Action action) {
        actionsByUuid.put(uuid, action);
    }
}
