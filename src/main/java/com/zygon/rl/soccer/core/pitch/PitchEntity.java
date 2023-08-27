package com.zygon.rl.soccer.core.pitch;

import com.zygon.rl.soccer.core.Identifable;

import java.util.Objects;

/**
 *
 * @author zygon
 */
public class PitchEntity implements Identifable {

    private final String id;

    public PitchEntity(String id) {
        this.id = id;
    }

    @Override
    public final String getId() {
        return this.id;
    }

    // Kind of a weak way to implement this check.
    public boolean isBall() {
        return false;
    }

    @Override
    public String toString() {
        return getId();
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PitchEntity other = (PitchEntity) obj;
        return Objects.equals(this.id, other.id);
    }
}
