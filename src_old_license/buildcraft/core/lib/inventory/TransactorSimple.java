/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.lib.inventory;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

import buildcraft.api.core.IInvSlot;
import buildcraft.api.core.IStackFilter;

import buildcraft.lib.inventory.InventoryIterator;
import buildcraft.lib.misc.StackUtil;

public class TransactorSimple extends Transactor {
    protected IInventory inventory;
    protected EnumFacing orientation;

    public TransactorSimple(IInventory inventory, EnumFacing orientation) {
        this.inventory = inventory;
        this.orientation = orientation;
    }

    @Override
    public int inject(ItemStack stack, boolean doAdd) {
        List<IInvSlot> filledSlots = new ArrayList<>(inventory.getSizeInventory());
        List<IInvSlot> emptySlots = new ArrayList<>(inventory.getSizeInventory());
        for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
            if (slot.canPutStackInSlot(stack)) {
                if (slot.getStackInSlot() == null) {
                    emptySlots.add(slot);
                } else {
                    filledSlots.add(slot);
                }
            }
        }

        int injected = 0;
        injected = tryPut(stack, filledSlots, injected, doAdd);
        injected = tryPut(stack, emptySlots, injected, doAdd);

        if (injected > 0 && doAdd) {
            inventory.markDirty();
        }
        return injected;
    }

    private int tryPut(ItemStack stack, List<IInvSlot> slots, int injected, boolean doAdd) {
        int realInjected = injected;

        if (realInjected >= stack.stackSize) {
            return realInjected;
        }

        for (IInvSlot slot : slots) {
            ItemStack stackInSlot = slot.getStackInSlot();
            if (stackInSlot == null || StackUtil.canMerge(stackInSlot, stack)) {
                int used = addToSlot(slot, stack, realInjected, doAdd);
                if (used > 0) {
                    realInjected += used;
                    if (realInjected >= stack.stackSize) {
                        return realInjected;
                    }
                }
            }
        }

        return realInjected;
    }

    /** @param slot
     * @param stack
     * @param injected Amount not to move?
     * @param doAdd
     * @return Return the number of items moved. */
    protected int addToSlot(IInvSlot slot, ItemStack stack, int injected, boolean doAdd) {
        int available = stack.stackSize - injected;
        int max = Math.min(stack.getMaxStackSize(), inventory.getInventoryStackLimit());

        ItemStack stackInSlot = slot.getStackInSlot();
        if (stackInSlot == null) {
            int wanted = Math.min(available, max);
            if (doAdd) {
                stackInSlot = stack.copy();
                stackInSlot.stackSize = wanted;
                slot.setStackInSlot(stackInSlot);
            }
            return wanted;
        }

        if (!StackUtil.canMerge(stack, stackInSlot)) {
            return 0;
        }

        int wanted = max - stackInSlot.stackSize;
        if (wanted <= 0) {
            return 0;
        }

        if (wanted > available) {
            wanted = available;
        }

        if (doAdd) {
            stackInSlot.stackSize += wanted;
            slot.setStackInSlot(stackInSlot);
        }
        return wanted;
    }

    @Override
    public ItemStack remove(IStackFilter filter, boolean doRemove) {
        for (IInvSlot slot : InventoryIterator.getIterable(inventory, orientation)) {
            ItemStack stack = slot.getStackInSlot();
            if (stack != null && slot.canTakeStackFromSlot(stack) && filter.matches(stack)) {
                if (doRemove) {
                    return slot.decreaseStackInSlot(1);
                } else {
                    ItemStack output = stack.copy();
                    output.stackSize = 1;
                    return output;
                }
            }
        }
        return null;
    }
}

