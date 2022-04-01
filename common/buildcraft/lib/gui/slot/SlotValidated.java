/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.lib.gui.slot;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotValidated extends Slot {

    public SlotValidated(IInventory inv, int id, int x, int y) {
        super(inv, id, x, y);
    }

    @Override
    public boolean isItemValid(ItemStack itemStack) {
        return inventory.isItemValidForSlot(this.getSlotIndex(), itemStack);
    }
}
