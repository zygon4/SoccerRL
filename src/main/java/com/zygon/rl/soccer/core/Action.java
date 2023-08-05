package com.zygon.rl.soccer.core;

import com.zygon.rl.soccer.utils.Pair;

import java.util.UUID;
import java.util.function.Function;

/**
 *
 * @author zygon
 */
public abstract class Action {

    @Deprecated
    private final Pair<String, Function<String, String>> argumentPromptAndValidator;
    private final UUID uuid;

    protected Action(
            Pair<String, Function<String, String>> argumentPromptAndValidator) {
        this.uuid = UUID.randomUUID();
        this.argumentPromptAndValidator = argumentPromptAndValidator;
    }

    protected Action() {
        this(null);
    }

    public final UUID getUuid() {
        return uuid;
    }

    public abstract String getDisplayString();

    public boolean hasArgument() {
        return argumentPromptAndValidator != null;
    }

    public String getArgumentPrompt() {
        return argumentPromptAndValidator.getLeft();
    }

    public String getArgumentError(String input) {
        return argumentPromptAndValidator.getRight().apply(input);
    }
}
