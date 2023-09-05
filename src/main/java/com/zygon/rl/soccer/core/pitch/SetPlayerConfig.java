package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Location;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Moving an entity anywhere from anywhere as long as the original space has
 * something and the destination is empty.
 */
public class SetPlayerConfig extends Pitch.PitchAction {

    private final PlayerEntity player;
    private final Location destination;
    private final boolean highlight;
    private final String displayString;

    public SetPlayerConfig(PlayerEntity player, Location destination,
            boolean highlight) {
        this.player = Objects.requireNonNull(player);
        this.destination = destination;
        this.highlight = highlight;

        List<String> configChanges = List.of(
                "SET DEST TO " + this.destination,
                "SET HIGHLIGHT TO " + this.highlight);
        this.displayString = configChanges.stream()
                .collect(Collectors.joining(",", this.player.toString() + ": ", ""));
    }

    /**
     * Sets destination, does not affect other config settings.
     */
    public SetPlayerConfig(PlayerEntity player, Location destination) {
        this(player, destination, player.isHighlighted());
    }

    /**
     * Sets high-lighting, does not affect other config settings.
     */
    public SetPlayerConfig(PlayerEntity player, boolean highlight) {
        this(player, player.getDestination(), highlight);
    }

    @Override
    public boolean canExecute(Pitch pitch) {
        return true;
    }

    @Override
    public void doExecute(Pitch pitch) {
        if (!canExecute(pitch)) {
            throw new IllegalStateException(getDisplayString());
        }
        player.setDestination(destination);
        player.setHighlighted(highlight);
        pitch.save(player, pitch.getLocation(player));
    }

    @Override
    public String getDisplayString() {
        return displayString;
    }
}
