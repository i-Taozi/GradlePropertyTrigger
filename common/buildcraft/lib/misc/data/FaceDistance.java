package buildcraft.lib.misc.data;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumFacing.Axis;

import buildcraft.lib.misc.VecUtil;

public final class FaceDistance {
    public final EnumFacing direction;
    public final int distance;

    public FaceDistance(Axis axis, int distance) {
        this.direction = VecUtil.getFacing(axis, distance > 0);
        this.distance = Math.abs(distance);
    }

    public FaceDistance(EnumFacing direction, int distance) {
        this.direction = direction;
        this.distance = distance;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((direction == null) ? 0 : direction.hashCode());
        result = prime * result + distance;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        FaceDistance other = (FaceDistance) obj;
        if (direction != other.direction) return false;
        if (distance != other.distance) return false;
        return true;
    }

    @Override
    public String toString() {
        return "FaceDistance [direction=" + direction + ", distance=" + distance + "]";
    }
}
