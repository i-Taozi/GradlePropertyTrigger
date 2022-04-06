package com.dynious.refinedrelocation.tileentity;

import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.tileentity.ISortingMember;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingMemberHandler;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;

public class TileSortingConnector extends TileEntity implements ISortingMember, IDisguisable
{
    public Block blockDisguisedAs = null;
    public int blockDisguisedMetadata = 0;
    private ISortingMemberHandler sortingHandler = APIUtils.createSortingMemberHandler(this);
    private boolean isFirstTick = true;

    public boolean onActivated(EntityPlayer player, int side)
    {
        return false;
    }

    @Override
    public boolean canDisguise()
    {
        return true;
    }

    @Override
    public boolean canDisguiseAs(Block block, int metadata)
    {
        return block.isOpaqueCube();
    }

    @Override
    public Block getDisguise()
    {
        return blockDisguisedAs;
    }

    @Override
    public int getDisguiseMeta()
    {
        return blockDisguisedMetadata;
    }

    @Override
    public void setDisguise(Block block, int metadata)
    {
        blockDisguisedAs = block;
        blockDisguisedMetadata = metadata;
        if (worldObj != null)
        {
            worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            worldObj.markTileEntityChunkModified(this.xCoord, this.yCoord, this.zCoord, this);
        }
    }

    @Override
    public void clearDisguise()
    {
        setDisguise(null, 0);
    }

    @Override
    public void updateEntity()
    {
        if (isFirstTick)
        {
            getHandler().onTileAdded();
            isFirstTick = false;
        }
        super.updateEntity();
    }

    @Override
    public ISortingMemberHandler getHandler()
    {
        return sortingHandler;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        int disguiseBlockId = compound.getInteger("disguisedId");
        if (disguiseBlockId != 0)
        {
            int disguisedMeta = compound.getInteger("disguisedMeta");
            setDisguise(Block.getBlockById(disguiseBlockId), disguisedMeta);
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        if (blockDisguisedAs != null)
        {
            compound.setInteger("disguisedId", Block.getIdFromBlock(blockDisguisedAs));
            compound.setInteger("disguisedMeta", blockDisguisedMetadata);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        int disguiseBlockId = pkt.field_148860_e.getInteger("disguisedId");
        if (disguiseBlockId != 0)
        {
            int disguisedMeta = pkt.field_148860_e.getInteger("disguisedMeta");
            setDisguise(Block.getBlockById(disguiseBlockId), disguisedMeta);
        }
    }

    @Override
    public Packet getDescriptionPacket()
    {
        NBTTagCompound compound = new NBTTagCompound();
        if (blockDisguisedAs != null)
        {
            compound.setInteger("disguisedId", Block.getIdFromBlock(blockDisguisedAs));
            compound.setInteger("disguisedMeta", blockDisguisedMetadata);
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, compound);
    }

    @Override
    public void invalidate()
    {
        getHandler().onTileRemoved();
        isFirstTick = true;
        super.invalidate();
    }

    @Override
    public void onChunkUnload()
    {
        getHandler().onTileRemoved();
        isFirstTick = true;
        super.onChunkUnload();
    }
}
