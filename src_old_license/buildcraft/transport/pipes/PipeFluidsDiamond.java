/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.HashSet;
import java.util.Set;
import io.netty.buffer.ByteBuf;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftTransport;
import buildcraft.api.core.IIconProvider;
import buildcraft.core.GuiIds;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.utils.FluidUtils;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.transport.BlockGenericPipe;
import buildcraft.transport.IDiamondPipe;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;
import buildcraft.transport.pipes.events.PipeEventFluid;

public class PipeFluidsDiamond extends Pipe<PipeTransportFluids> implements IDiamondPipe {

    private class FilterInventory extends SimpleInventory {
        public boolean[] filteredDirections = new boolean[6];
        public Fluid[] fluids = new Fluid[54];

        public FilterInventory(int size, String invName, int invStackLimit) {
            super(size, invName, invStackLimit);
        }

        @Override
        public boolean isItemValidForSlot(int slot, ItemStack stack) {
            return stack == null || FluidUtils.isFluidContainer(stack);
        }

        @Override
        public void markDirty() {
            // calculate fluid cache
            for (int i = 0; i < 6; i++) {
                filteredDirections[i] = false;
            }

            for (int i = 0; i < 54; i++) {
                fluids[i] = FluidUtils.getFluidFromItemStack(getStackInSlot(i));
                if (fluids[i] != null) {
                    filteredDirections[i / 9] = true;
                }
            }
        }
    }

    private FilterInventory filters = new FilterInventory(54, "Filters", 1);

    public PipeFluidsDiamond(Item item) {
        super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
    }

    @Override
    public IInventory getFilters() {
        return filters;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        return PipeIconProvider.diamondPipeFluids.get(direction).ordinal();
    }

    @Override
    public int getIconIndexForItem() {
        return PipeIconProvider.TYPE.PipeFluidsDiamond_Item.ordinal();
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer, EnumFacing direction) {
        if (entityplayer.getCurrentEquippedItem() != null) {
            if (Block.getBlockFromItem(entityplayer.getCurrentEquippedItem().getItem()) instanceof BlockGenericPipe) {
                return false;
            }
        }

        if (!container.getWorld().isRemote) {
            entityplayer.openGui(BuildCraftTransport.instance, GuiIds.PIPE_DIAMOND, container.getWorld(), container.getPos().getX(), container
                    .getPos().getY(), container.getPos().getZ());
        }

        return true;
    }

    public void eventHandler(PipeEventFluid.FindDest event) {
        Fluid fluidInTank = event.fluidStack.getFluid();
        Set<EnumFacing> originalDestinations = new HashSet<>();
        originalDestinations.addAll(event.destinations.elementSet());
        boolean isFiltered = false;
        int[] filterCount = new int[6];

        for (EnumFacing dir : originalDestinations) {
            if (container.isPipeConnected(dir) && filters.filteredDirections[dir.ordinal()]) {
                for (int slot = dir.ordinal() * 9; slot < dir.ordinal() * 9 + 9; ++slot) {
                    if (filters.fluids[slot] != null && filters.fluids[slot].getID() == fluidInTank.getID()) {
                        filterCount[dir.ordinal()]++;
                        isFiltered = true;
                    }
                }
            }
        }

        event.destinations.clear();

        if (!isFiltered) {
            for (EnumFacing to : originalDestinations) {
                if (!filters.filteredDirections[to.ordinal()]) {
                    event.destinations.add(to);
                }
            }
        } else {
            for (EnumFacing to : originalDestinations) {
                if (filterCount[to.ordinal()] > 0) {
                    event.destinations.add(to, filterCount[to.ordinal()]);
                }
            }
        }
    }

    /* SAVING & LOADING */
    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        filters.readFromNBT(nbt);
        filters.markDirty();
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        filters.writeToNBT(nbt);
    }

    // ICLIENTSTATE
    @Override
    public void writeData(ByteBuf data) {
        NBTTagCompound nbt = new NBTTagCompound();
        filters.writeToNBT(nbt);
        NetworkUtils.writeNBT(data, nbt);
    }

    @Override
    public void readData(ByteBuf data) {
        NBTTagCompound nbt = NetworkUtils.readNBT(data);
        filters.readFromNBT(nbt);
        filters.markDirty();
    }
}
