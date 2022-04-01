package buildcraft.robotics.map;

import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

import buildcraft.api.core.BCLog;

public class MapChunk {
    private static final int VERSION = 1;

    private int x, z;
    private byte[] data;

    public MapChunk(int x, int z) {
        this.x = x;
        this.z = z;
        data = new byte[256];
    }

    public MapChunk(NBTTagCompound compound) {
        readFromNBT(compound);
    }

    public int getX() {
        return x;
    }

    public int getZ() {
        return z;
    }

    public int getColor(int x, int z) {
        return (int) data[((z & 15) << 4) | (x & 15)];
    }

    public void update(Chunk chunk) {
        for (int bz = 0; bz < 16; bz++) {
            for (int bx = 0; bx < 16; bx++) {
                int y = chunk.getHeightValue(bx, bz);
                int color = MapColor.AIR.colorIndex;

                if (y < 0) {
                    y = 255;
                }

                Block b;
                IBlockState state;

                while (y >= 0) {
                    state = chunk.getBlockState(new BlockPos(bx, y, bz));
                    b = state.getBlock();

                    color = state.getMapColor() != null ? state.getMapColor().colorIndex : MapColor.AIR.colorIndex;
                    if (color != MapColor.AIR.colorIndex) {
                        break;
                    }
                    y--;
                }

                data[(bz << 4) | bx] = (byte) color;
            }
        }
    }

    public void readFromNBT(NBTTagCompound compound) {
        int version = compound.getShort("version");
        if (version > MapChunk.VERSION) {
            BCLog.logger.error("Unsupported MapChunk version: " + version);
            return;
        }
        x = compound.getInteger("x");
        z = compound.getInteger("z");
        data = compound.getByteArray("data");
        if (data.length != 256) {
            BCLog.logger.error("Invalid MapChunk data length: " + data.length);
            data = new byte[256];
        }
    }

    public void writeToNBT(NBTTagCompound compound) {
        compound.setShort("version", (short) VERSION);
        compound.setInteger("x", x);
        compound.setInteger("z", z);
        compound.setByteArray("data", data);
    }

    @Override
    public int hashCode() {
        return 31 * x + z;
    }
}
