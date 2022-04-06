package com.dynious.refinedrelocation.tileentity;

import com.dynious.refinedrelocation.api.APIUtils;
import com.dynious.refinedrelocation.api.filter.IFilterGUI;
import com.dynious.refinedrelocation.api.tileentity.IMultiFilterTile;
import com.dynious.refinedrelocation.api.tileentity.ISortingInventory;
import com.dynious.refinedrelocation.api.tileentity.ISortingMember;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingInventoryHandler;
import com.dynious.refinedrelocation.helper.DirectionHelper;
import com.dynious.refinedrelocation.helper.IOHelper;
import com.dynious.refinedrelocation.lib.Names;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;

public class TileSortingInterface extends TileSortingConnector implements ISortingInventory, IMultiFilterTile
{
    public ItemStack[] bufferInventory = new ItemStack[1];
    private ISortingInventoryHandler sortingHandler = APIUtils.createSortingInventoryHandler(this);
    private IFilterGUI filter = APIUtils.createStandardFilter(this);
    private boolean isStuffed = false;
    private int counter;
    private ForgeDirection connectedSide = ForgeDirection.UNKNOWN;
    private Priority priority = Priority.NORMAL;

    @Override
    public boolean onActivated(EntityPlayer player, int side)
    {
        if (!worldObj.isRemote)
        {
            APIUtils.openFilteringGUI(player, worldObj, xCoord, yCoord, zCoord);
            return true;
        }
        return false;
    }

    @Override
    public ISortingInventoryHandler getHandler()
    {
        return sortingHandler;
    }

    @Override
    public ItemStack putInInventory(ItemStack itemStack, boolean simulate)
    {
        if (connectedSide != ForgeDirection.UNKNOWN)
        {
            TileEntity tile = DirectionHelper.getTileAtSide(worldObj, xCoord, yCoord, zCoord, connectedSide);
            if (tile != null && !(tile instanceof ISortingMember))
            {
                itemStack = IOHelper.insert(tile, itemStack, connectedSide.getOpposite(), simulate);
                if (itemStack == null || itemStack.stackSize == 0)
                    return null;
            }
        }
        return itemStack;
    }

    @Override
    public boolean putStackInSlot(ItemStack itemStack, int slotIndex)
    {
        itemStack = putInInventory(itemStack, false);
        bufferInventory[0] = itemStack;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
        return true;
    }

    @Override
    public Priority getPriority()
    {
        return priority;
    }

    @Override
    public void setPriority(Priority priority)
    {
        this.priority = priority;
    }

    @Override
    public IFilterGUI getFilter()
    {
        return filter;
    }

    @Override
    public void onFilterChanged()
    {
        this.markDirty();
    }

    @Override
    public TileEntity getTileEntity()
    {
        return this;
    }

    @Override
    public void updateEntity()
    {
        super.updateEntity();
        if (bufferInventory[0] != null)
        {
            counter++;
            if (counter % 22 == 0)
            {
                bufferInventory[0] = putInInventory(bufferInventory[0], false);
                if (bufferInventory[0] == null)
                {
                    worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
                }
            }
        }
    }

    public boolean isStuffed()
    {
        return isStuffed;
    }

    @Override
    public int getSizeInventory()
    {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int i)
    {
        return bufferInventory[i];
    }

    @Override
    public ItemStack decrStackSize(int slotIndex, int decrementAmount)
    {
        ItemStack itemStack = getStackInSlot(slotIndex);
        if (itemStack != null)
        {
            if (itemStack.stackSize <= decrementAmount)
            {
                setInventorySlotContents(slotIndex, null);
            }
            else
            {
                itemStack = itemStack.splitStack(decrementAmount);
                if (itemStack.stackSize == 0)
                {
                    setInventorySlotContents(slotIndex, null);
                }
            }
        }

        return itemStack;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slotIndex)
    {
        ItemStack itemStack = getStackInSlot(slotIndex);
        if (itemStack != null)
        {
            setInventorySlotContents(slotIndex, null);
        }
        return itemStack;
    }

    @Override
    public void setInventorySlotContents(int slotIndex, ItemStack itemStack)
    {
        sortingHandler.setInventorySlotContents(slotIndex, itemStack);
    }

    @Override
    public String getInventoryName()
    {
        return Names.sortingInterface;
    }

    @Override
    public boolean hasCustomInventoryName()
    {
        return false;
    }

    @Override
    public int getInventoryStackLimit()
    {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer)
    {
        return true;
    }

    @Override
    public void openInventory()
    {
    }

    @Override
    public void closeInventory()
    {
    }

    @Override
    public boolean isItemValidForSlot(int i, ItemStack itemstack)
    {
        return bufferInventory[0] == null;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound)
    {
        super.readFromNBT(compound);
        filter.readFromNBT(compound);
        setConnectedSide(ForgeDirection.getOrientation(compound.getByte("side")));
        if (compound.hasKey("priority"))
        {
            setPriority(Priority.values()[compound.getByte("priority")]);
        }
        else
        {
            setPriority(Priority.NORMAL);
        }
        if (compound.hasKey("Items"))
        {
            NBTTagList tagList = compound.getTagList("Items", 10);
            this.bufferInventory[0] = ItemStack.loadItemStackFromNBT(tagList.getCompoundTagAt(0));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound compound)
    {
        super.writeToNBT(compound);
        filter.writeToNBT(compound);
        compound.setByte("side", (byte) connectedSide.ordinal());
        compound.setByte("priority", (byte) priority.ordinal());
        if (bufferInventory[0] != null)
        {
            NBTTagList nbttaglist = new NBTTagList();
            NBTTagCompound tag = new NBTTagCompound();
            this.bufferInventory[0].writeToNBT(tag);
            nbttaglist.appendTag(tag);
            compound.setTag("Items", nbttaglist);
        }
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt)
    {
        super.onDataPacket(net, pkt);
        setConnectedSide(ForgeDirection.getOrientation(pkt.func_148857_g().getByte("side")));
        isStuffed = pkt.func_148857_g().getBoolean("stuffed");
    }

    @Override
    public Packet getDescriptionPacket()
    {
        S35PacketUpdateTileEntity pkt = (S35PacketUpdateTileEntity) super.getDescriptionPacket();
        NBTTagCompound tag = pkt.field_148860_e;
        tag.setByte("side", (byte) connectedSide.ordinal());
        tag.setBoolean("stuffed", bufferInventory[0] != null);
        return pkt;
    }

    public boolean rotateBlock()
    {
        setConnectedSide(ForgeDirection.getOrientation((connectedSide.ordinal() + 1) % ForgeDirection.VALID_DIRECTIONS.length));
        return true;
    }

    public ForgeDirection getConnectedSide()
    {
        return connectedSide;
    }

    public void setConnectedSide(ForgeDirection direction)
    {
        this.connectedSide = direction;
        if (worldObj != null)
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }
}
