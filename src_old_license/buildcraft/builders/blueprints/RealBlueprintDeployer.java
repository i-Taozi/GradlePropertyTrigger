/** Copyright (c) 2011-2015, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * <p/>
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt */
package buildcraft.builders.blueprints;

import java.io.File;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import buildcraft.api.blueprints.BlueprintDeployer;
import buildcraft.builders.LibraryDatabase;
import buildcraft.core.blueprints.Blueprint;
import buildcraft.core.blueprints.BlueprintBase;
import buildcraft.core.blueprints.BptBuilderBlueprint;
import buildcraft.core.blueprints.BptContext;
import buildcraft.core.blueprints.LibraryId;
import buildcraft.core.lib.utils.Utils;
import buildcraft.lib.misc.NBTUtilBC;

public class RealBlueprintDeployer extends BlueprintDeployer {
    public static final RealBlueprintDeployer realInstance = new RealBlueprintDeployer();

    @Override
    public void deployBlueprint(World world, BlockPos pos, EnumFacing dir, File file) {
        deployBlueprint(world, pos, dir, (Blueprint) BlueprintBase.loadBluePrint(LibraryDatabase.load(file)));
    }

    @Override
    public void deployBlueprintFromFileStream(World world, BlockPos pos, EnumFacing dir, byte[] data) {
        deployBlueprint(world, pos, dir, (Blueprint) BlueprintBase.loadBluePrint(NBTUtilBC.load(data)));
    }

    public void deployBlueprint(World world, BlockPos pos, EnumFacing dir, Blueprint bpt) {
        bpt.id = new LibraryId();
        bpt.id.extension = "bpt";

        BptContext context = bpt.getContext(world, bpt.getBoxForPos(pos));

        if (bpt.rotate) {
            if (dir == EnumFacing.EAST) {
                // Do nothing
            } else if (dir == EnumFacing.SOUTH) {
                bpt.rotateLeft(context);
            } else if (dir == EnumFacing.WEST) {
                bpt.rotateLeft(context);
                bpt.rotateLeft(context);
            } else if (dir == EnumFacing.NORTH) {
                bpt.rotateLeft(context);
                bpt.rotateLeft(context);
                bpt.rotateLeft(context);
            }
        }

        Vec3d transform = Utils.convert(pos).subtract(Utils.convert(bpt.anchor));

        bpt.translateToWorld(transform);

        new BptBuilderBlueprint(bpt, world, pos).deploy();
    }
}
