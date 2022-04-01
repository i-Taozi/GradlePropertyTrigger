/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pipes;

import java.util.List;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import cofh.api.energy.IEnergyReceiver;
import buildcraft.BuildCraftTransport;
import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IIconProvider;
import buildcraft.api.core.ISerializable;
import buildcraft.api.tiles.IDebuggable;
import buildcraft.api.transport.IPipeTile;
import buildcraft.transport.Pipe;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.PipeTransportFluids;

public class PipeFluidsWood extends Pipe<PipeTransportFluids> implements IEnergyReceiver, ISerializable, IDebuggable {
    private static final int ENERGY_MULTIPLIER = 50;

    public int fluidToExtract;

    protected int standardIconIndex = PipeIconProvider.TYPE.PipeFluidsWood_Standard.ordinal();
    protected int solidIconIndex = PipeIconProvider.TYPE.PipeFluidsWood_Solid.ordinal();

    private PipeLogicWood logic = new PipeLogicWood(this) {
        @Override
        protected boolean isValidConnectingTile(TileEntity tile) {
            if (tile instanceof IPipeTile) {
                return false;
            }
            if (!(tile instanceof IFluidHandler)) {
                return false;
            }

            return true;
        }
    };

    public PipeFluidsWood(Item item) {
        super(new PipeTransportFluids(), item);

        transport.initFromPipe(getClass());
    }

    @Override
    public boolean blockActivated(EntityPlayer entityplayer, EnumFacing side) {
        return logic.blockActivated(entityplayer, EnumPipePart.fromFacing(side));
    }

    @Override
    public void onNeighborBlockChange(int blockId) {
        logic.onNeighborBlockChange();
        super.onNeighborBlockChange(blockId);
    }

    @Override
    public void initialize() {
        logic.initialize();
        super.initialize();
    }

    private TileEntity getConnectingTile() {
        int meta = container.getBlockMetadata();
        return meta >= 6 ? null : container.getTile(EnumFacing.getFront(meta));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (fluidToExtract <= 0) {
            return;
        }

        TileEntity tile = getConnectingTile();

        if (tile == null || !(tile instanceof IFluidHandler)) {
            fluidToExtract = 0;
        } else {
            extractFluid((IFluidHandler) tile, EnumFacing.getFront(container.getBlockMetadata()));

            // We always subtract the flowRate to ensure that the buffer goes down reasonably quickly.
            fluidToExtract -= transport.getFlowRate();

            if (fluidToExtract < 0) {
                fluidToExtract = 0;
            }
        }
    }

    public int extractFluid(IFluidHandler fluidHandler, EnumFacing side) {
        int amount = fluidToExtract > transport.getFlowRate() ? transport.getFlowRate() : fluidToExtract;
        FluidTankInfo tankInfo = transport.getTankInfo(side)[0];
        FluidStack extracted;

        if (tankInfo.fluid != null) {
            extracted = fluidHandler.drain(side.getOpposite(), new FluidStack(tankInfo.fluid, amount), false);
        } else {
            extracted = fluidHandler.drain(side.getOpposite(), amount, false);
        }

        int inserted = 0;

        if (extracted != null) {
            inserted = transport.fill(side, extracted, true);
            if (inserted > 0) {
                extracted.amount = inserted;
                fluidHandler.drain(side.getOpposite(), extracted, true);
            }
        }

        return inserted;
    }

    protected int getEnergyMultiplier() {
        return 5 * BuildCraftTransport.pipeFluidsBaseFlowRate;
    }

    protected int getMaxExtractionFluid() {
        return 100 * BuildCraftTransport.pipeFluidsBaseFlowRate;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIconProvider getIconProvider() {
        return BuildCraftTransport.instance.pipeIconProvider;
    }

    @Override
    public int getIconIndex(EnumFacing direction) {
        if (direction == null) {
            return standardIconIndex;
        } else {
            int metadata = container.getBlockMetadata();

            if (metadata == direction.ordinal()) {
                return solidIconIndex;
            } else {
                return standardIconIndex;
            }
        }
    }

    @Override
    public boolean outputOpen(EnumFacing to) {
        int meta = container.getBlockMetadata();
        return super.outputOpen(to) && meta != to.ordinal();
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return true;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        TileEntity tile = getConnectingTile();
        if (tile == null || !(tile instanceof IFluidHandler)) {
            return 0;
        }

        int maxToReceive = (getMaxExtractionFluid() - fluidToExtract) / getEnergyMultiplier();
        int received = Math.min(maxReceive, maxToReceive);
        if (!simulate) {
            fluidToExtract += getEnergyMultiplier() * received;
        }
        return received;
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        return 0;
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        return 1000 / getEnergyMultiplier();
    }

    @Override
    public void writeData(ByteBuf data) {
        data.writeShort(fluidToExtract);
    }

    @Override
    public void readData(ByteBuf data) {
        fluidToExtract = data.readShort();
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, EnumFacing side) {
        left.add("");
        left.add("PipeFluidsWood");
        left.add(" Fluid Extraction Potential = " + fluidToExtract + "mB");
    }
}
