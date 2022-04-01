/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.block;

import java.util.HashSet;

import org.apache.commons.lang3.StringUtils;

import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.ReportedException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.InvWrapper;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.BCLog;
import buildcraft.api.core.ISerializable;
import buildcraft.api.tiles.IControllable;
import buildcraft.api.tiles.IControllable.Mode;
import buildcraft.core.DefaultProps;
import buildcraft.core.lib.BlockTileCache;
import buildcraft.core.lib.RFBattery;
import buildcraft.core.lib.network.PacketTileUpdate;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.lib.misc.NBTUtilBC;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/** For future maintainers: This class intentionally does not implement just every interface out there. For some of them
 * (such as IControllable), we expect the tiles supporting it to implement it - but TileBuildCraft provides all the
 * underlying functionality to stop code repetition. */
public abstract class TileBuildCraft extends TileEntity implements IEnergyProvider, IEnergyReceiver, ISerializable, ITickable, IAdditionalDataTile {
    protected BlockTileCache[] cache;
    protected HashSet<EntityPlayer> guiWatchers = new HashSet<>();
    protected IControllable.Mode mode;
    private boolean sendNetworkUpdate = false;

    protected int init = 0;
    private String owner = "[BuildCraft]";
    private RFBattery battery;

    private int receivedTick, extractedTick;
    private long worldTimeEnergyReceive;
    /** Used at the client for the power LED brightness */
    public int ledPower = 0, lastLedPower = 0;
    public boolean ledDone = false, lastLedDone = false;

    /** Used to help migrate existing worlds to whatever new blockstate format we use. Note that proper migration cannot
     * be implemented until this pre-release has gone out for a while now. */
    private NBTTagCompound lastBlockState = null;

    public String getOwner() {
        return owner;
    }

    public void addGuiWatcher(EntityPlayer player) {
        if (!guiWatchers.contains(player)) {
            guiWatchers.add(player);
        }
    }

    public void removeGuiWatcher(EntityPlayer player) {
        if (guiWatchers.contains(player)) {
            guiWatchers.remove(player);
        }
    }

    @Override
    public void update() {
        if (worldObj == null) throw new NullPointerException("worldObj");
        if (init != 2 && !isInvalid()) {
            if (init < 1) {
                init++;
                return;
            }
            initialize();
            init = 2;
        }

        if (battery != null) {
            receivedTick = 0;
            extractedTick = 0;

            if (!worldObj.isRemote) {
                int prePower = ledPower;
                int stored = battery.getEnergyStored();
                int max = battery.getMaxEnergyStored();
                ledPower = 0;
                if (stored != 0) {
                    ledPower = stored * 2 / max + 1;
                }
                if (prePower != ledPower) {
                    sendNetworkUpdate();
                }
            }
        }

        if (!worldObj.isRemote) {
            if (battery != null) {
                if (battery.getMaxEnergyStored() > 0) {
                    ledPower = 3 * battery.getEnergyStored() / battery.getMaxEnergyStored();
                } else {
                    ledPower = 0;
                }
            }
        }

        if (lastLedPower != ledPower || lastLedDone != ledDone) {
            if (worldObj.isRemote) {
                worldObj.markBlockForUpdate(getPos());
            } else {
                sendNetworkUpdate();
            }
            lastLedPower = ledPower;
            lastLedDone = ledDone;
        }

        if (sendNetworkUpdate) {
            if (worldObj != null && !worldObj.isRemote) {
                BuildCraftCore.instance.sendToPlayers(getPacketUpdate(), worldObj, getPos(), DefaultProps.NETWORK_UPDATE_RANGE);
                sendNetworkUpdate = false;
            }
        }
    }

    public void initialize() {

    }

    @Override
    public void validate() {
        super.validate();
        cache = null;
    }

    @Override
    public void invalidate() {
        init = 0;
        super.invalidate();
        cache = null;
    }

    public void onBlockPlacedBy(EntityLivingBase entity, ItemStack stack) {
        if (entity instanceof EntityPlayer) {
            owner = ((EntityPlayer) entity).getDisplayNameString();
        }
    }

    public void destroy() {
        cache = null;
    }

    @Override
    public void sendNetworkUpdate() {
        sendNetworkUpdate = true;
    }

    @Override
    public void writeData(ByteBuf stream) {
        stream.writeByte(ledPower);
        NetworkUtils.writeEnum(stream, mode);
    }

    @Override
    public void readData(ByteBuf stream) {
        ledPower = stream.readByte();
        mode = NetworkUtils.readEnum(stream, Mode.class);
    }

    public PacketTileUpdate getPacketUpdate() {
        return new PacketTileUpdate(this, this);
    }

    @Override
    public SPacketUpdateTileEntity getUpdatePacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("net-type", "desc-packet");
        Packet p = getPacketUpdate();
        ByteBuf buf = Unpooled.buffer();
        p.writeData(buf);
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        nbt.setByteArray("net-data", bytes);
        SPacketUpdateTileEntity tileUpdate = new SPacketUpdateTileEntity(getPos(), 0, nbt);
        return tileUpdate;
    }

    @Override
    public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
        if (!worldObj.isRemote) return;
        if (pkt.getNbtCompound() == null) throw new RuntimeException("No NBTTag compound! This is a bug!");
        NBTTagCompound nbt = pkt.getNbtCompound();
        try {
            if ("desc-packet".equals(nbt.getString("net-type"))) {
                byte[] bytes = nbt.getByteArray("net-data");
                ByteBuf data = Unpooled.wrappedBuffer(bytes);
                PacketTileUpdate p = new PacketTileUpdate();
                p.readData(data);
                // The player is not used so its fine
                p.applyData(worldObj, null);
            } else {
                BCLog.logger.warn("Recieved a packet with a different type that expected (" + nbt.getString("net-type") + ")");
            }
        } catch (Throwable t) {
            throw new RuntimeException("Failed to read a packet! (net-type=\"" + nbt.getTag("net-type") + "\", net-data=\"" + nbt.getTag("net-data") + "\")", t);
        }
    }

    @Override
    public boolean shouldRefresh(World world, BlockPos pos, IBlockState oldState, IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("owner", owner);
        if (battery != null) {
            NBTTagCompound batteryNBT = new NBTTagCompound();
            battery.writeToNBT(batteryNBT);
            nbt.setTag("battery", batteryNBT);
        }
        if (mode != null) {
            nbt.setByte("lastMode", (byte) mode.ordinal());
        }

        // Version tag that can be used for upgrading.
        // 0 means[1.8.9] 7.2.0-pre12 or before (default value)
        // 1 means [1.8.9] 7.2.0-pre13 up until 7.2.0
        // 2 means [1.9] 8.0.0 or later
        nbt.setInteger("data-version", 1);

        /* Also save the state of all BC tiles. This will be helpful for migration. */
        // REMOVE THIS AFTER preX
        if (hasWorldObj()) {
            IBlockState blockstate = worldObj.getBlockState(getPos());
            Block block = blockstate.getBlock();
            if (block instanceof BlockBuildCraft) {
                // Assume that this is us- it would be odd for this tile to be with the wrong block.
                BlockBuildCraft bcBlock = (BlockBuildCraft) block;
                NBTTagCompound statenbt = new NBTTagCompound();
                for (IProperty<?> prop : bcBlock.properties) {
                    Object value = blockstate.getValue(prop);
                    if (value == null) continue;
                    statenbt.setTag(prop.getName(), NBTUtilBC.writeObject(value));
                }
                nbt.setTag("blockstate", statenbt);
            }
        }

        return nbt;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("owner")) {
            owner = nbt.getString("owner");
        }
        if (battery != null) {
            battery.readFromNBT(nbt.getCompoundTag("battery"));
        }
        if (nbt.hasKey("lastMode")) {
            mode = IControllable.Mode.values()[nbt.getByte("lastMode")];
        }

        int version = nbt.getInteger("data-version");

        // Load up the block from pre12 -> preX
        if (nbt.hasKey("blockstate") && version == 1) lastBlockState = nbt.getCompoundTag("blockstate");
    }

    protected int getTicksSinceEnergyReceived() {
        return (int) (worldObj.getTotalWorldTime() - worldTimeEnergyReceive);
    }

    @Override
    public int hashCode() {
        return pos.hashCode();
    }

    @Override
    public boolean equals(Object cmp) {
        return this == cmp;
    }

    @Override
    public boolean canConnectEnergy(EnumFacing from) {
        return battery != null;
    }

    @Override
    public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {
        if (battery != null && this.canConnectEnergy(from)) {
            int received = battery.receiveEnergy(Math.min(maxReceive, battery.getMaxEnergyReceive() - receivedTick), simulate);
            if (!simulate) {
                receivedTick += received;
                worldTimeEnergyReceive = worldObj.getTotalWorldTime();
            }
            return received;
        } else {
            return 0;
        }
    }

    /** If you want to use this, implement IEnergyProvider. */
    @Override
    public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {
        if (battery != null && this.canConnectEnergy(from)) {
            int extracted = battery.extractEnergy(Math.min(maxExtract, battery.getMaxEnergyExtract() - extractedTick), simulate);
            if (!simulate) {
                extractedTick += extracted;
            }
            return extracted;
        } else {
            return 0;
        }
    }

    @Override
    public int getEnergyStored(EnumFacing from) {
        if (battery != null && this.canConnectEnergy(from)) {
            return battery.getEnergyStored();
        } else {
            return 0;
        }
    }

    @Override
    public int getMaxEnergyStored(EnumFacing from) {
        if (battery != null && this.canConnectEnergy(from)) {
            return battery.getMaxEnergyStored();
        } else {
            return 0;
        }
    }

    public RFBattery getBattery() {
        return battery;
    }

    protected void setBattery(RFBattery battery) {
        this.battery = battery;
    }

    public IBlockState getBlockState(EnumFacing side) {
        if (isNotReady()) return null;
        if (cache == null) {
            cache = BlockTileCache.makeCache(worldObj, pos, false);
        }
        return cache[side.ordinal()].getBlockState();
    }

    public TileEntity getTile(EnumFacing side) {
        if (isNotReady()) return null;
        if (cache == null) {
            cache = BlockTileCache.makeCache(worldObj, pos, false);
        }
        return cache[side.ordinal()].getTile();
    }

    public IControllable.Mode getControlMode() {
        return mode;
    }

    public void setControlMode(IControllable.Mode mode) {
        this.mode = mode;
        sendNetworkUpdate();
    }

    // Capability wrapper

    private IItemHandler[] invWrapper;

    @Override
    public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            return this instanceof IInventory;
        } else {
            return super.hasCapability(capability, facing);
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
        if (capability == CapUtil.CAP_ITEMS) {
            if (this instanceof IInventory) {
                if (invWrapper == null) {
                    if (this instanceof ISidedInventory) {
                        invWrapper = new IItemHandler[7];
                        for (EnumFacing facing1 : EnumFacing.VALUES) {
                            invWrapper[facing1.ordinal()] = new SidedInvWrapper((ISidedInventory) this, facing);
                        }
                        invWrapper[6] = new SidedInvWrapper((ISidedInventory) this, null);
                    } else {
                        invWrapper = new IItemHandler[1];
                        invWrapper[0] = new InvWrapper((IInventory) this);
                    }
                }

                if (invWrapper.length == 7) {
                    return (T) invWrapper[facing == null ? 6 : facing.ordinal()];
                } else {
                    return (T) invWrapper[0];
                }
            }
            return null;
        } else {
            return super.getCapability(capability, facing);
        }
    }

    // IInventory

    public int getField(int id) {
        return 0;
    }

    public void setField(int id, int value) {}

    public int getFieldCount() {
        return 0;
    }

    public String getInventoryName() {
        return "";
    }

    public String getName() {
        return getInventoryName();
    }

    public ITextComponent getDisplayName() {
        return new TextComponentString(getInventoryName());
    }

    public void clear() {}

    public boolean hasCustomName() {
        return !StringUtils.isEmpty(getInventoryName());
    }

    public boolean isNotReady() {
        return !hasWorldObj() || init != 2;
    }
}
