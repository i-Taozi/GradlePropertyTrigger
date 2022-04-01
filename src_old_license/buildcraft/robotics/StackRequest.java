/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license, which
 * should be located as "LICENSE.API" in the BuildCraft source code distribution. */
package buildcraft.robotics;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.robots.DockingStation;
import buildcraft.api.robots.IRequestProvider;
import buildcraft.api.robots.IRobotRegistry;
import buildcraft.api.robots.ResourceId;
import buildcraft.api.robots.ResourceIdRequest;
import buildcraft.api.robots.RobotManager;
import buildcraft.lib.misc.NBTUtilBC;

public class StackRequest {
    private IRequestProvider requester;

    private int slot;

    private ItemStack stack;

    private DockingStation station;
    private BlockPos stationIndex;
    private EnumFacing stationSide;

    public StackRequest(IRequestProvider requester, int slot, ItemStack stack) {
        this.requester = requester;
        this.slot = slot;
        this.stack = stack;
        this.station = null;
    }

    private StackRequest(int slot, ItemStack stack, BlockPos stationIndex, EnumFacing stationSide) {
        requester = null;
        this.slot = slot;
        this.stack = stack;
        station = null;
        this.stationIndex = stationIndex;
        this.stationSide = stationSide;
    }

    public IRequestProvider getRequester(World world) {
        if (requester == null) {
            DockingStation dockingStation = getStation(world);
            if (dockingStation != null) {
                requester = dockingStation.getRequestProvider();
            }
        }
        return requester;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    public DockingStation getStation(World world) {
        if (station == null) {
            IRobotRegistry robotRegistry = RobotManager.registryProvider.getRegistry(world);
            station = robotRegistry.getStation(stationIndex, stationSide);
        }
        return station;
    }

    public void setStation(DockingStation station) {
        this.station = station;
        this.stationIndex = station.index();
        this.stationSide = station.side();
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setInteger("slot", slot);

        NBTTagCompound stackNBT = new NBTTagCompound();
        stack.writeToNBT(stackNBT);
        nbt.setTag("stack", stackNBT);

        if (station != null) {
            nbt.setTag("stationIndex", NBTUtilBC.writeBlockPos(stationIndex));
            nbt.setByte("stationSide", (byte) station.side().ordinal());
        }
    }

    public static StackRequest loadFromNBT(NBTTagCompound nbt) {
        if (nbt.hasKey("stationIndex")) {
            int slot = nbt.getInteger("slot");

            ItemStack stack = ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"));

            BlockPos stationIndex = NBTUtilBC.readBlockPos(nbt.getTag("stationIndex"));
            EnumFacing stationSide = EnumFacing.values()[nbt.getByte("stationSide")];

            return new StackRequest(slot, stack, stationIndex, stationSide);
        } else {
            return null;
        }
    }

    public ResourceId getResourceId(World world) {
        return getStation(world) != null ? new ResourceIdRequest(getStation(world), slot) : null;
    }
}
