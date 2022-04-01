/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.utils;

import java.util.Iterator;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;

public class BlockScanner implements Iterable<BlockPos> {

    Box box = new Box();
    World world;

    BlockPos pos;
    int iterationsPerCycle;
    int blocksDone = 0;

    class BlockIt implements Iterator<BlockPos> {

        int it = 0;

        @Override
        public boolean hasNext() {
            return pos.getZ() <= box.max().getZ() && it <= iterationsPerCycle;
        }

        @Override
        public BlockPos next() {
            BlockPos index = new BlockPos(pos);
            it++;
            blocksDone++;

            if (pos.getX() < box.max().getX()) {
                pos = pos.east();
            } else {
                pos = new BlockPos(box.min().getX(), pos.getY(), pos.getZ());

                if (pos.getY() < box.max().getY()) {
                    pos = pos.up();
                } else {
                    pos = new BlockPos(pos.getX(), box.min().getY(), pos.getZ() + 1);
                }
            }

            return index;
        }

        @Override
        public void remove() {

        }
    }

    public BlockScanner(Box box, World world, int iterationsPreCycle) {
        this.box = box;
        this.world = world;
        this.iterationsPerCycle = iterationsPreCycle;
        pos = box.min();
    }

    public BlockScanner() {}

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockIt();
    }

    public int totalBlocks() {
        return box.size().getX() * box.size().getY() * box.size().getZ();
    }

    public int blocksLeft() {
        return totalBlocks() - blocksDone;
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setTag("pos", NBTUtilBC.writeBlockPos(pos));
        nbt.setInteger("blocksDone", blocksDone);
        nbt.setInteger("iterationsPerCycle", iterationsPerCycle);
        NBTTagCompound boxNBT = new NBTTagCompound();
        box.writeToNBT(boxNBT);
        nbt.setTag("box", boxNBT);
    }

    public void readFromNBT(NBTTagCompound nbt) {
        pos = NBTUtilBC.readBlockPos(nbt.getTag("pos"));
        blocksDone = nbt.getInteger("blocksDone");
        iterationsPerCycle = nbt.getInteger("iterationsPerCycle");
        box.initialize(nbt.getCompoundTag("box"));
    }

}
