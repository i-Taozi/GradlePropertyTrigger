/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IChatComponent;

/** Allows you to deal with multiple inventories through a single interface. */
public final class InventoryConcatenator implements IInventory {

    private final List<Integer> slotMap = new ArrayList<>();
    private final List<IInventory> invMap = new ArrayList<>();

    private InventoryConcatenator() {}

    public static InventoryConcatenator make() {
        return new InventoryConcatenator();
    }

    public InventoryConcatenator add(IInventory inv) {
        for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
            slotMap.add(slot);
            invMap.add(inv);
        }
        return this;
    }

    @Override
    public int getSizeInventory() {
        return slotMap.size();
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return invMap.get(slot).getStackInSlot(slotMap.get(slot));
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        return invMap.get(slot).decrStackSize(slotMap.get(slot), amount);
    }

    @Override
    public ItemStack removeStackFromSlot(int slot) {
        return invMap.get(slot).removeStackFromSlot(slotMap.get(slot));
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        invMap.get(slot).setInventorySlotContents(slotMap.get(slot), stack);
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer entityplayer) {
        return true;
    }

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        return invMap.get(slot).isItemValidForSlot(slotMap.get(slot), stack);
    }

    @Override
    public void markDirty() {
        for (IInventory inv : invMap) {
            inv.markDirty();
        }
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public boolean hasCustomName() {
        return false;
    }

    @Override
    public IChatComponent getDisplayName() {
        return null;
    }

    @Override
    public void openInventory(EntityPlayer player) {}

    @Override
    public void closeInventory(EntityPlayer player) {}

    @Override
    public int getField(int id) {
        return 0;
    }

    @Override
    public void setField(int id, int value) {}

    @Override
    public int getFieldCount() {
        return 0;
    }

    @Override
    public void clear() {}
}
