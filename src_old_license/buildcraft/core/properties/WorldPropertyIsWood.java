/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.core.properties;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.oredict.OreDictionary;

public class WorldPropertyIsWood extends WorldProperty {

    private int woodId = 0;

    public WorldPropertyIsWood() {
        woodId = OreDictionary.getOreID("logWood");
    }

    @Override
    public boolean get(IBlockAccess blockAccess, IBlockState state, BlockPos pos) {
        Block block = state.getBlock();
        if (block == null) {
            return false;
        } else {
            ItemStack stack = new ItemStack(block);

            if (stack.getItem() != null) {
                for (int id : OreDictionary.getOreIDs(stack)) {
                    if (id == woodId) {
                        return true;
                    }
                }
            }
        }

        return false;
    }
}
