/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.transport.pluggable;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

import buildcraft.api.transport.IPipe;
import buildcraft.api.transport.pluggable.IPipePluggableItem;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.core.lib.items.ItemBuildCraft;

public class ItemPowerAdapter extends ItemBuildCraft implements IPipePluggableItem {
    public ItemPowerAdapter() {
        super();
    }

    @Override
    public String getUnlocalizedName(ItemStack itemstack) {
        return "item.PipePowerAdapter";
    }

    @Override
    public boolean doesSneakBypassUse(World world, BlockPos pos, EntityPlayer player) {
        return true;
    }

    @Override
    public PipePluggable createPipePluggable(IPipe pipe, EnumFacing side, ItemStack stack) {
        return new PowerAdapterPluggable();
    }

}
