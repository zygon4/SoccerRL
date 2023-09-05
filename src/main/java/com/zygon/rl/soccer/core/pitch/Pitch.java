package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.GenericEntityManager;
import com.zygon.rl.soccer.core.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * The pitch is intended to be the world physics.
 */
public class Pitch {

    // a model for refactoring other actions into this style
    public static abstract class PitchAction extends Action {

        public abstract boolean canExecute(Pitch pitch);

        public final void execute(Pitch pitch) {
            System.out.println("Executing " + getDisplayString());
            try {
                doExecute(pitch);
            } catch (Throwable th) {
                th.printStackTrace();
                // TODO: error handling..
            }
        }

        protected abstract void doExecute(Pitch pitch);
    }

    public static final int PITCH_SCALE = 2;

    // total field
    public static final int FIELD_HEIGHT = 35 * PITCH_SCALE;
    public static final int FIELD_WIDTH = 25 * PITCH_SCALE;

    // game field
    public static final int PITCH_HEIGHT = 30 * PITCH_SCALE;
    public static final int PITCH_WIDTH = 20 * PITCH_SCALE;

    public static final int PITCH_FIELD_HEIGHT_OFFSET = 5;
    public static final int PITCH_FIELD_WIDTH_OFFSET = 5;

    private final GenericEntityManager<PitchEntity> pitchEntites = new GenericEntityManager<>();

    private final List<Location> changes = new ArrayList<>();

    /**
     * Returns all the locations that have had a change since the last call with
     * the reset flag set to true.
     *
     * @param reset if true, will return the current set of entries and then
     * clear the log.
     * @return
     */
    public List<Location> getLocationChanges(boolean reset) {
        List<Location> imChanges = new ArrayList<>(changes);
        if (reset) {
            changes.clear();
        }
        return imChanges;
    }

    /**
     * Returns the nearest neighbors.
     *
     * @param location
     * @return
     */
    public Collection<Location> getNeighborLocations(Location location) {
        return location.getNeighbors(1);
    }

    /////////// ECM methods /////////////
    public List<PitchEntity> get(Location location,
            Predicate<PitchEntity> filter) {
        return getPitchEntites().get(location, filter);
    }

    void delete(PitchEntity id, Location location) {
        getPitchEntites().delete(id, location);
        changes.add(location);
    }

    void save(PitchEntity entity, Location location) {
        getPitchEntites().save(entity, location);
        changes.add(location);
    }

    /////////// ECM methods /////////////
    public boolean hasBall(PlayerEntity player) {
        return getBallLocation().equals(getLocation(player));
    }

    public boolean hasBall(Location location) {
        return getPitchEntites().get(location).stream()
                .filter(po -> po.isBall())
                .map(po -> (PitchBall) po)
                .findAny().isPresent();
    }

    public PlayerEntity getPlayer(Location location) {
        return (PlayerEntity) getPitchEntites().get(location).stream()
                .filter(po -> !po.isBall())
                .findAny().orElse(null);
    }

    public Set<Location> getPlayerLocations() {
        final GenericEntityManager<PitchEntity> entityManager = getPitchEntites();
        return entityManager.getLocations().stream()
                .filter(l -> !entityManager.get(l, pe -> !pe.isBall()).isEmpty())
                .collect(Collectors.toSet());
    }

    public PitchBall getBall() {
        return (PitchBall) getPitchEntites().get(getBallLocation(), PitchEntity::isBall).get(0);
    }

    public Location getBallLocation() {
        return getPitchEntites().getLocations().stream()
                .filter(this::hasBall)
                .findFirst().orElse(null);
    }

    public Location getLocation(PlayerEntity player) {
        return getPitchEntites().getLocations().stream()
                .filter(l -> {
                    PlayerEntity p = getPlayer(l);
                    return p != null && p.equals(player);
                })
                .findFirst().orElse(null);
    }

    private GenericEntityManager<PitchEntity> getPitchEntites() {
        return pitchEntites;
    }

    // Went from void/throws to boolean
    static boolean validateLegalLocation(Location location) {
        if (location.getX() < 0 || location.getX() >= FIELD_WIDTH) {
            return false;
        }

        if (location.getY() < 0 || location.getY() >= FIELD_HEIGHT) {
            return false;
        }

        return true;
    }
}
