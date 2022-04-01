package buildcraft.lib.cache;

import javax.annotation.Nullable;

import net.minecraft.tileentity.TileEntity;

public final class TileCacheRet {

    /** If this is null then that means we *know* the tile wasn't found, and so callers don't need to bother looking in
     * the world manually. */
    @Nullable
    public final TileEntity tile;

    public TileCacheRet(TileEntity tile) {
        this.tile = tile;
    }
}
