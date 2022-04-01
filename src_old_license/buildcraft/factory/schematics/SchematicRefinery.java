/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.factory.schematics;

import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;

import buildcraft.BuildCraftFactory;
import buildcraft.api.blueprints.IBuilderContext;
import buildcraft.api.blueprints.SchematicTile;
import buildcraft.api.properties.BuildCraftProperties;

public class SchematicRefinery extends SchematicTile {

    @Override
    public void getRequirementsForPlacement(IBuilderContext context, List<ItemStack> requirements) {
        requirements.add(new ItemStack(BuildCraftFactory.refineryBlock));
    }

    @Override
    public void storeRequirements(IBuilderContext context, BlockPos pos) {

    }

    @Override
    public void rotateLeft(IBuilderContext context) {
        EnumFacing face = state.getValue(BuildCraftProperties.BLOCK_FACING).rotateY();
        state = state.withProperty(BuildCraftProperties.BLOCK_FACING, face);
    }

    @Override
    public void initializeFromObjectAt(IBuilderContext context, BlockPos pos) {
        super.initializeFromObjectAt(context, pos);

        tileNBT.removeTag("tank1");
        tileNBT.removeTag("tank2");
        tileNBT.removeTag("result");
        tileNBT.removeTag("battery");
    }

    @Override
    public void placeInWorld(IBuilderContext context, BlockPos pos, List<ItemStack> stacks) {
        // to support refineries coming from older blueprints
        tileNBT.removeTag("tank1");
        tileNBT.removeTag("tank2");
        tileNBT.removeTag("result");
        tileNBT.removeTag("battery");

        super.placeInWorld(context, pos, stacks);
    }

}
