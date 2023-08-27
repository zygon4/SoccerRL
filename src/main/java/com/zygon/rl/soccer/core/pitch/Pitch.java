package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Action;
import com.zygon.rl.soccer.core.GenericEntityManager;
import com.zygon.rl.soccer.core.Location;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
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
    public static final int HEIGHT = 30 * PITCH_SCALE;
    public static final int WIDTH = 20 * PITCH_SCALE;

    private final GenericEntityManager<PitchEntity> pitchEntites = new GenericEntityManager<>();

    private final List<String> log = new ArrayList<>();

    public List<String> getLog() {
        return Collections.unmodifiableList(log);
    }

    /**
     * Returns the nearest neighbors. HOWEVER, this only returns the n/s/e/w, no
     * diagonals. TODO: add diagonals.
     *
     * @param location
     * @return
     */
    public Collection<Location> getNeighborLocations(Location location) {
        return location.getNeighbors(1);
    }

    GenericEntityManager<PitchEntity> getPitchEntites() {
        return pitchEntites;
    }

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

    // from height of 0
//    private Set<Location> setPitch(Team team, boolean reversed) {
//
//        Iterator<Player> players = team.getPlayers().iterator();
//
//        FormationHelper helper = new FormationHelper(team.getFormation(), HEIGHT, WIDTH);
//        Set<Location> zoneLocations = helper.getPlayerPitchLocations(HEIGHT / 2);
//
//        for (Location loc : zoneLocations) {
//            Location trueLocation = loc;
//            if (reversed) {
//                int reverse = HEIGHT - loc.getY() - 1;
//                trueLocation = loc.setY(reverse);
//            }
//
//            Player player = null;
//            try {
//                player = players.next();
//            } catch (Throwable th) {
//                th.printStackTrace();
//            }
//
//            AddPitchEntity add = new AddPitchEntity(trueLocation, new PlayerObject(player));
//            if (add.canExecute(this)) {
//                add.execute(this);
//            } else {
//                throw new IllegalArgumentException();
//            }
//        }
//
//        return zoneLocations;
//    }
//
    // Went from void/throws to boolean
    static boolean validateLegalLocation(Location location) {
        if (location.getX() < 0 || location.getX() >= WIDTH) {
            return false;
        }

        if (location.getY() < 0 || location.getY() >= HEIGHT) {
            return false;
        }

        return true;
    }
}
