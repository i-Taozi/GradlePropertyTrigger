/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.network;

import io.netty.buffer.ByteBuf;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.core.lib.network.base.Packet;
import buildcraft.transport.PipeTransportItems;
import buildcraft.transport.TileGenericPipe;
import buildcraft.transport.TravelingItem;

public class PacketPipeTransportTraveler extends Packet {

    public BlockPos pos;

    private TravelingItem item;
    private boolean forceStackRefresh, toCenter;
    private int entityId;
    private EnumFacing input;
    private EnumFacing output;
    private EnumDyeColor color;
    private float itemPos;
    private float speed;

    public PacketPipeTransportTraveler() {}

    public PacketPipeTransportTraveler(TileEntity tile, TravelingItem item, boolean forceStackRefresh) {
        super(tile.getWorld());
        this.pos = tile.getPos();
        this.item = item;
        this.forceStackRefresh = forceStackRefresh;
    }

    @Override
    public void writeData(ByteBuf data) {
        super.writeData(data);
        data.writeInt(pos.getX());
        data.writeInt(pos.getY());
        data.writeInt(pos.getZ());

        data.writeFloat(item.pos);

        data.writeShort(item.id);

        int out = item.output == null ? 6 : item.output.ordinal();
        int in = item.input == null ? 6 : item.input.ordinal();

        byte flags = (byte) ((out & 7) | ((in & 7) << 3) | (forceStackRefresh ? 64 : 0) | (item.toCenter ? 128 : 0));
        data.writeByte(flags);

        data.writeByte(item.color != null ? item.color.ordinal() : -1);

        data.writeFloat(item.getSpeed());
    }

    @Override
    public void readData(ByteBuf data) {
        super.readData(data);
        pos = new BlockPos(data.readInt(), data.readInt(), data.readInt());
        itemPos = data.readFloat();

        this.entityId = data.readShort();

        int flags = data.readUnsignedByte();

        int in = (flags >> 3) & 7;
        if (in == 6) {
            this.input = null;
        } else {
            this.input = EnumFacing.getFront(in);
        }

        int out = flags & 7;
        if (out == 6) {
            this.output = null;
        } else {
            this.output = EnumFacing.getFront(out);
        }

        byte c = data.readByte();
        if (c != -1) {
            this.color = EnumDyeColor.byMetadata(c);
        }

        this.speed = data.readFloat();

        this.forceStackRefresh = (flags & 0x40) != 0;
        this.toCenter = (flags & 0x80) != 0;
    }

    public boolean getToCenter() {
        return toCenter;
    }

    public int getTravelingEntityId() {
        return entityId;
    }

    public EnumFacing getInputOrientation() {
        return input;
    }

    public EnumFacing getOutputOrientation() {
        return output;
    }

    public EnumDyeColor getColor() {
        return color;
    }

    public float getItemPos() {
        return itemPos;
    }

    public float getSpeed() {
        return speed;
    }

    public boolean forceStackRefresh() {
        return forceStackRefresh;
    }

    @Override
    public void applyData(World world, EntityPlayer player) {
        if (!world.isBlockLoaded(pos)) {
            return;
        }

        TileEntity entity = world.getTileEntity(pos);
        if (!(entity instanceof TileGenericPipe)) {
            return;
        }

        TileGenericPipe pipe = (TileGenericPipe) entity;
        if (pipe.pipe == null) {
            return;
        }

        if (!(pipe.pipe.transport instanceof PipeTransportItems)) {
            return;
        }

        ((PipeTransportItems) pipe.pipe.transport).handleTravelerPacket(this);
    }
}
