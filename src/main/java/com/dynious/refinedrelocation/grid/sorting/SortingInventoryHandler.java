package com.dynious.refinedrelocation.grid.sorting;

import com.dynious.refinedrelocation.api.tileentity.ISortingInventory;
import com.dynious.refinedrelocation.api.tileentity.handlers.ISortingInventoryHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class SortingInventoryHandler extends SortingMemberHandler implements ISortingInventoryHandler
{
    protected ISortingInventory inventory;

    public SortingInventoryHandler(TileEntity owner)
    {
        super(owner);
        this.inventory = (ISortingInventory) owner;
    }

    /**
     * Sets the given item stack to the specified slot in the inventory (can be crafting or armor sections).
     */
    public final void setInventorySlotContents(int par1, ItemStack itemStack)
    {
        if (itemStack == null || owner.getWorldObj().isRemote || getGrid() == null)
        {
            inventory.putStackInSlot(itemStack, par1);
            inventory.markDirty();
            return;
        }

        ItemStack originalStack = itemStack.copy();

        itemStack = getGrid().filterStackToGroup(itemStack.copy(), this.owner, par1, false);

        inventory.putStackInSlot(itemStack, par1);

        if (!ItemStack.areItemStacksEqual(originalStack, itemStack))
            syncInventory(itemStack, par1);

        inventory.markDirty();
    }

    private void syncInventory(ItemStack stack, int slot)
    {
        float checkSize = 5.0F;
        List list = this.owner.getWorldObj().getEntitiesWithinAABB(EntityPlayer.class, AxisAlignedBB.getBoundingBox((float) this.owner.xCoord - checkSize, (float) this.owner.yCoord - checkSize, (float) this.owner.zCoord - checkSize, (float) this.owner.xCoord + 1 + checkSize, (float) this.owner.yCoord + 1 + checkSize, (float) this.owner.zCoord + 1 + checkSize));

        for (Object aList : list)
        {
            EntityPlayer player = (EntityPlayer) aList;
            if (!(player.openContainer instanceof ContainerPlayer))
            {
                for (Slot invSlot : (List<Slot>) player.openContainer.inventorySlots)
                {
                    if (invSlot.inventory == inventory && invSlot.slotIndex == slot)
                    {
                        ((EntityPlayerMP) player).sendSlotContents(player.openContainer, slot, stack);
                        return;
                    }
                }
            }
        }
    }

    @Override
    public void onInventoryChange()
    {
        if (getGrid() != null && !getOwner().getWorldObj().isRemote)
            getGrid().onInventoryChange();
    }
}
