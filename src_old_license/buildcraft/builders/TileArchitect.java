/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders;

import java.util.ArrayList;
import java.util.LinkedList;
import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import buildcraft.BuildCraftCore;
import buildcraft.api.core.IAreaProvider;
import buildcraft.builders.blueprints.RecursiveBlueprintReader;
import buildcraft.core.LaserData;
import buildcraft.core.blueprints.BlueprintReadConfiguration;
import buildcraft.core.internal.IBoxProvider;
import buildcraft.core.lib.block.TileBuildCraft;
import buildcraft.core.lib.inventory.SimpleInventory;
import buildcraft.core.lib.network.base.Packet;
import buildcraft.core.lib.network.command.CommandWriter;
import buildcraft.core.lib.network.command.ICommandReceiver;
import buildcraft.core.lib.network.command.PacketCommand;
import buildcraft.core.lib.utils.NetworkUtils;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtilBC;
import buildcraft.lib.misc.data.Box;
import buildcraft.lib.misc.data.Box.Kind;

public class TileArchitect extends TileBuildCraft implements IInventory, IBoxProvider, ICommandReceiver {
    public enum Mode {
        NONE,
        EDIT,
        COPY
    }

    public String currentAuthorName = "";
    public Mode mode = Mode.NONE;

    public Box box = new Box();
    public String name = "";
    public BlueprintReadConfiguration readConfiguration = new BlueprintReadConfiguration();

    public LinkedList<LaserData> subLasers = new LinkedList<>();

    public ArrayList<BlockPos> subBlueprints = new ArrayList<>();

    private SimpleInventory inv = new SimpleInventory(2, "Architect", 1);
    private RecursiveBlueprintReader reader;
    private boolean clientIsWorking, initialized;

    public TileArchitect() {
        box.kind = Kind.BLUE_STRIPES;
    }

    public void storeBlueprintStack(ItemStack blueprintStack) {
        setInventorySlotContents(1, blueprintStack);
        decrStackSize(0, 1);
    }

    @Override
    public void update() {
        super.update();

        if (!worldObj.isRemote) {
            if (mode == Mode.COPY && reader != null) {
                reader.iterate();

                if (reader.isDone()) {
                    reader = null;
                    sendNetworkUpdate();
                }
            }
        }
    }

    @Override
    public void initialize() {
        super.initialize();

        if (!worldObj.isRemote && !initialized) {
            if (!box.isInitialized()) {
                IAreaProvider a = Utils.getNearbyAreaProvider(worldObj, pos);

                if (a != null) {
                    mode = Mode.COPY;
                    box.initialize(a);
                    a.removeFromWorld();
                    sendNetworkUpdate();
                    return;
                } else {
                    if (BuildCraftCore.DEVELOPER_MODE) {
                        mode = Mode.EDIT;
                    } else {
                        mode = Mode.NONE;
                    }
                }
            } else {
                mode = Mode.COPY;
            }
            initialized = true;
            sendNetworkUpdate();
        }
    }

    @Override
    public int getSizeInventory() {
        return 2;
    }

    @Override
    public ItemStack getStackInSlot(int i) {
        return inv.getStackInSlot(i);
    }

    @Override
    public ItemStack decrStackSize(int i, int j) {
        ItemStack result = inv.decrStackSize(i, j);

        if (i == 0) {
            initializeBlueprint();
        }

        return result;
    }

    @Override
    public void setInventorySlotContents(int i, ItemStack itemstack) {
        inv.setInventorySlotContents(i, itemstack);

        if (i == 0) {
            initializeBlueprint();
        }
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return inv.removeStackFromSlot(slot);
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return mode != Mode.NONE && worldObj.getTileEntity(pos) == this;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        if (nbt.hasKey("box")) {
            box.initialize(nbt.getCompoundTag("box"));
        }

        inv.readFromNBT(nbt);

        mode = Mode.values()[nbt.getByte("mode")];
        name = nbt.getString("name");
        currentAuthorName = nbt.getString("lastAuthor");

        if (nbt.hasKey("readConfiguration")) {
            readConfiguration.readFromNBT(nbt.getCompoundTag("readConfiguration"));
        }

        NBTTagList subBptList = nbt.getTagList("subBlueprints", Constants.NBT.TAG_COMPOUND);

        for (int i = 0; i < subBptList.tagCount(); ++i) {
            BlockPos index = NBTUtilBC.readBlockPos(subBptList.getCompoundTagAt(i));

            addSubBlueprint(index);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (box.isInitialized()) {
            NBTTagCompound boxStore = new NBTTagCompound();
            box.writeToNBT(boxStore);
            nbt.setTag("box", boxStore);
        }

        inv.writeToNBT(nbt);

        nbt.setByte("mode", (byte) mode.ordinal());
        nbt.setString("name", name);
        nbt.setString("lastAuthor", currentAuthorName);

        NBTTagCompound readConf = new NBTTagCompound();
        readConfiguration.writeToNBT(readConf);
        nbt.setTag("readConfiguration", readConf);

        NBTTagList subBptList = new NBTTagList();

        for (BlockPos b : subBlueprints) {
            subBptList.appendTag(NBTUtilBC.writeBlockPos(b));
        }

        nbt.setTag("subBlueprints", subBptList);
    }

    private boolean getIsWorking() {
        return mode == Mode.COPY ? reader != null : false;
    }

    @Override
    public void writeData(ByteBuf stream) {
        box.writeData(stream);
        NetworkUtils.writeUTF(stream, name);
        stream.writeBoolean(getIsWorking());
        stream.writeByte(mode.ordinal());
        if (mode == Mode.COPY) {
            readConfiguration.writeData(stream);
            stream.writeShort(subLasers.size());
            for (LaserData ld : subLasers) {
                ld.writeData(stream);
            }
        }
    }

    @Override
    public void readData(ByteBuf stream) {
        box.readData(stream);
        name = NetworkUtils.readUTF(stream);
        clientIsWorking = stream.readBoolean();
        mode = Mode.values()[stream.readByte()];

        if (mode == Mode.COPY) {
            readConfiguration.readData(stream);
            int size = stream.readUnsignedShort();
            subLasers.clear();
            for (int i = 0; i < size; i++) {
                LaserData ld = new LaserData();
                ld.readData(stream);
                subLasers.add(ld);
            }
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        destroy();
    }

    private void initializeBlueprint() {
        if (getWorld().isRemote) {
            return;
        }

        if (mode == Mode.COPY) {
            reader = new RecursiveBlueprintReader(this);
        }
        sendNetworkUpdate();
    }

    public int getComputingProgressScaled(int scale) {
        if (reader != null) {
            return (int) (reader.getComputingProgressScaled() * scale);
        } else {
            return 0;
        }
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public boolean hasCustomName() {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int var1, ItemStack var2) {
        return false;
    }

    @Override
    public Box getBox() {
        return box;
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        Box completeBox = new Box(this).extendToEncompass(box);

        for (LaserData d : subLasers) {
            completeBox.extendToEncompass(d.tail);
        }

        return completeBox.getBoundingBox();
    }

    public Packet getPacketSetName() {
        return new PacketCommand(this, "setName", new CommandWriter() {
            @Override
            public void write(ByteBuf data) {
                NetworkUtils.writeUTF(data, name);
            }
        });
    }

    @Override
    public void receiveCommand(String command, Side side, Object sender, ByteBuf stream) {
        if ("setName".equals(command)) {
            this.name = NetworkUtils.readUTF(stream);
            if (side.isServer()) {
                BuildCraftCore.instance.sendToPlayersNear(getPacketSetName(), this);
            }
        } else if (side.isServer()) {
            if ("setReadConfiguration".equals(command)) {
                readConfiguration.readData(stream);
                sendNetworkUpdate();
            }
        }
    }

    public void rpcSetConfiguration(BlueprintReadConfiguration conf) {
        readConfiguration = conf;

        BuildCraftCore.instance.sendToServer(new PacketCommand(this, "setReadConfiguration", new CommandWriter() {
            @Override
            public void write(ByteBuf data) {
                readConfiguration.writeData(data);
            }
        }));
    }

    public void addSubBlueprint(TileEntity sub) {
        if (mode == Mode.COPY) {
            addSubBlueprint(sub.getPos());
            sendNetworkUpdate();
        }
    }

    private void addSubBlueprint(BlockPos index) {
        subBlueprints.add(index);

        Vec3d point5 = new Vec3d(0.5, 0.5, 0.5);

        LaserData laser = new LaserData(Utils.convert(index).add(point5), Utils.convert(this.getPos()).add(point5));

        subLasers.add(laser);
    }

    @Override
    public String getInventoryName() {
        return "Template";
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }
}
