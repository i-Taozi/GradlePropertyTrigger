/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.schematics;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicBlock;

public class SchematicPortal extends SchematicBlock {

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {

    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {

    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {}

    @Override
    public boolean isAlreadyBuilt(IBuilderContext context, BlockPos pos) {
        return true;
    }

    @Override
    public void postProcessing(IBuilderContext context, BlockPos pos) {
        context.world().setBlockState(pos, state);
    }
}
